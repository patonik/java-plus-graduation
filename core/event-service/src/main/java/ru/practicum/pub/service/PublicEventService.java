package ru.practicum.pub.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.interaction.client.RequestClient;
import ru.practicum.interaction.dto.event.*;
import ru.practicum.interaction.dto.event.request.RequestCount;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Event;
import ru.practicum.interaction.util.EventDtoByDateTimeComparator;
import ru.practicum.interaction.util.StatParams;
import ru.practicum.interaction.util.Statistical;
import ru.practicum.pub.repository.PublicEventRepository;
import ru.practicum.stats.HttpStatsClient;
import ru.practicum.stats.dto.StatRequestDto;
import ru.practicum.stats.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PublicEventService {
    private final PublicEventRepository publicEventRepository;
    private final HttpStatsClient httpStatsClient;
    private final EventFullDtoMapper eventFullDtoMapper;
    private final RequestClient requestClient;

    public EventFullDto getEvent(Long id, HttpServletRequest request) {
        Event event = publicEventRepository.findByIdAndState(id, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
        sendHitToStatsService(request);
        RequestCount requestCount = null;
        try {
            requestCount = requestClient.getAllConfirmedRequestsForEvent(event.getInitiatorId(), event.getId()).getBody();
            if (requestCount == null) {
                throw new RuntimeException("Error trying to count requests");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e);
        }
        EventFullDto eventFullDto = eventFullDtoMapper.toDto(event,
                requestCount.getConfirmedRequests(), 0L);
        StatParams statParams = Statistical.getParams(List.of(eventFullDto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Long hits = 0L;
        if (!statResponseDtoList.isEmpty()) {
            hits = statResponseDtoList.getFirst().getHits();
        }
        eventFullDto.setViews(hits);
        return eventFullDto;
    }

    public List<EventShortDto> getEvents(String text, Long[] categories, Boolean paid, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Boolean onlyAvailable, SortCriterium sort,
                                         Integer from, Integer size, HttpServletRequest request) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ConstraintViolationException("Range start is after range end", Set.of());
        }
        Pageable pageable = PageRequest.of(from, size);
        log.info(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        List<EventShortDto> shortDtos =
                publicEventRepository.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        for (EventShortDto shortDto : shortDtos) {
            RequestCount requestCount = null;
            try {
                requestCount = requestClient.getAllConfirmedRequestsForEvent(shortDto.getInitiator().getId(), shortDto.getId()).getBody();
                if (requestCount == null) {
                    throw new RuntimeException("Error trying to count requests");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Long confirmedRequests = requestCount.getConfirmedRequests();
            shortDto.setConfirmedRequests(confirmedRequests);
        }
        if (shortDtos.isEmpty()) {
            sendHitToStatsService(request);
            return shortDtos;
        }
        sendHitToStatsService(request);
        StatParams statParams = Statistical.getParams(new ArrayList<>(shortDtos));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Map<Long, Long> hitMap = statResponseDtoList
                .stream()
                .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        shortDtos.sort(Comparator.comparingLong(EventShortDto::getId));
        for (EventShortDto eventShortDto : shortDtos) {
            long hits = 0L;
            Long eventId = eventShortDto.getId();
            if (!hitMap.isEmpty() && hitMap.containsKey(eventId)) {
                hits = hitMap.get(eventId);
            }
            eventShortDto.setViews(hits);
        }
        switch (sort) {
            case null:
                break;
            case SortCriterium.VIEWS:
                shortDtos.sort(Comparator.comparingLong(EventShortDto::getViews));
                break;
            case SortCriterium.EVENT_DATE:
                shortDtos.sort(new EventDtoByDateTimeComparator());
                break;
        }
        return shortDtos;
    }

    private void sendHitToStatsService(HttpServletRequest request) {
        StatRequestDto hit = StatRequestDto.builder()
                .app("public-event-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();
        httpStatsClient.sendHit(hit, StatRequestDto.class);
    }

    public EventFullDto getInternalEvent(Long id) {
        Event event = publicEventRepository.findByIdAndState(id, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found with id: " + id));
        RequestCount requestCount = null;
        try {
            requestCount = requestClient.getAllConfirmedRequestsForEvent(event.getInitiatorId(), event.getId()).getBody();
            if (requestCount == null) {
                throw new RuntimeException("Error trying to count requests");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e);
        }
        EventFullDto eventFullDto = eventFullDtoMapper.toDto(event,
                requestCount.getConfirmedRequests(), 0L);
        StatParams statParams = Statistical.getParams(List.of(eventFullDto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDtoList =
                httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Long hits = 0L;
        if (!statResponseDtoList.isEmpty()) {
            hits = statResponseDtoList.getFirst().getHits();
        }
        eventFullDto.setViews(hits);
        return eventFullDto;
    }
}
