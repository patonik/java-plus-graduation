package ru.practicum.pub.repository;

import feign.FeignException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.client.RequestClient;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.request.RequestCount;
import ru.practicum.interaction.model.Compilation;
import ru.practicum.interaction.util.StatParams;
import ru.practicum.interaction.util.Statistical;
import ru.practicum.stats.HttpStatsClient;
import ru.practicum.stats.dto.StatResponseDto;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class CompilationDtoRepositoryImpl implements CompilationDtoRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Compilation> findAllCompilations(Boolean pinned, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Compilation> cq = cb.createQuery(Compilation.class);
        Root<Compilation> root = cq.from(Compilation.class);
        root.fetch("events", JoinType.LEFT);
        if (pinned != null) {
            cq.where(cb.equal(root.get("pinned"), pinned));
        }

        TypedQuery<Compilation> query = em.createQuery(cq);
        int pageSize = pageable.getPageSize();
        query.setFirstResult(pageable.getPageNumber() * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    @Override
    public void populateEventShortDtos(Set<EventShortDto> eventShortDtos,
                                       HttpStatsClient httpStatsClient, RequestClient requestClient) {
        // sort by EventShortDtos by id and put into map
        Map<Long, EventShortDto> sortedDtoMap =
                eventShortDtos.stream().sorted(Comparator.comparingLong(EventShortDto::getId)).collect(
                        Collectors.toMap(EventShortDto::getId, Function.identity(), (x, y) -> {
                            throw new RuntimeException("Duplicate key");
                        }, LinkedHashMap::new));
        log.info("Populated EventShortDtos: {}", sortedDtoMap);

        // get confirmed request amounts for each id in the map sorted by id
        populateConfirmedRequests(sortedDtoMap, requestClient);
        log.info("confReq populated");

        // get map of hits for each event id (no entries for events without views)
        StatParams statParams = Statistical.getParams(new ArrayList<>(eventShortDtos));
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Map<Long, Long> hitMap = statResponseDtoList
                .stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        log.info("Populated hitMap: {}", hitMap);

        //populate EventShortDtos with confirmed request amounts and views
        for (EventShortDto eventShortDto : sortedDtoMap.values()) {
            Long eventId = eventShortDto.getId();
            eventShortDto.setViews(hitMap.getOrDefault(eventId, 0L));
        }
    }

    private void populateConfirmedRequests(Map<Long, EventShortDto> sortedDto, RequestClient requestClient) {
        for (Long l : sortedDto.keySet()) {
            RequestCount requestCount = null;
            try {
                EventShortDto eventShortDto = sortedDto.get(l);
                requestCount = requestClient.getAllConfirmedRequestsForEvent(eventShortDto.getInitiator().getId(),
                        l).getBody();
                if (requestCount == null) {
                    eventShortDto.setConfirmedRequests(0L);
                } else {
                    eventShortDto.setConfirmedRequests(requestCount.getConfirmedRequests());
                }
            } catch (FeignException.NotFound e) {
                throw new RuntimeException(e);
            }
        }
    }
}
