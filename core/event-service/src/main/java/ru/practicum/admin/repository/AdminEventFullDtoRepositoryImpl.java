package ru.practicum.admin.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.State;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.model.Event;
import ru.practicum.interaction.model.Locus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminEventFullDtoRepositoryImpl implements AdminEventFullDtoRepository {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<EventFullDto> getEventsOrderedById(List<Long> users,
                                                   List<State> states,
                                                   List<Long> categories,
                                                   List<Long> loci,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<EventFullDto> cq = cb.createQuery(EventFullDto.class);
        Root<Event> root = cq.from(Event.class);

        List<Predicate> predicates = getPredicates(cb, cq, root, users, states, categories, loci, rangeStart, rangeEnd);

        cq.select(cb.construct(EventFullDto.class,
                root.get("id"),
                root.get("annotation"),
                cb.construct(CategoryDto.class, root.get("category").get("id"),
                        root.get("category").get("name")),
                root.get("createdOn"),
                root.get("publishedOn"),
                root.get("description"),
                root.get("eventDate"),
                cb.construct(UserShortDto.class, root.get("initiatorId"),
                        root.get("initiatorName")),
                root.get("location"),
                root.get("paid"),
                root.get("participantLimit"),
                root.get("requestModeration"),
                root.get("title"),
                root.get("state"),
                cb.nullLiteral(Long.class),
                cb.nullLiteral(Long.class))
        );
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }
        cq.orderBy(cb.asc(root.get("id")));
        int pageSize = pageable.getPageSize();
        TypedQuery<EventFullDto> eventFullDtoTypedQuery =
                em.createQuery(cq).setFirstResult(pageable.getPageNumber() * pageSize)
                        .setMaxResults(pageSize);
        return eventFullDtoTypedQuery.getResultList();
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb,
                                          CriteriaQuery<EventFullDto> cq,
                                          Root<Event> root,
                                          List<Long> users,
                                          List<State> states,
                                          List<Long> categories,
                                          List<Long> loci,
                                          LocalDateTime rangeStart,
                                          LocalDateTime rangeEnd) {
        List<Predicate> predicates = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            predicates.add(root.get("initiatorId").in(users));
        }
        if (states != null && !states.isEmpty()) {
            predicates.add(root.get("state").in(states));
        }
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").get("id").in(categories));
        }
        if (loci != null && !loci.isEmpty()) {
            Subquery<Long> locusSubquery = cq.subquery(Long.class);
            Root<Locus> locusRoot = locusSubquery.from(Locus.class);
            locusSubquery.select(locusRoot.get("id"));
            locusSubquery.where(
                    cb.and(
                            locusRoot.get("id").in(loci),
                            cb.lessThanOrEqualTo(
                                    cb.function("distance",
                                            Float.class,
                                            locusRoot.get("lat"),
                                            locusRoot.get("lon"),
                                            root.get("location").get("lat"),
                                            root.get("location").get("lon")
                                    ),
                                    locusRoot.get("rad")
                            )
                    )
            );
            Expression<Long> selection = locusSubquery.getSelection();
            predicates.add(selection.isNotNull());
        }
        if (rangeStart != null && rangeEnd != null) {
            predicates.add(cb.between(root.get("eventDate"), rangeStart, rangeEnd));
        } else if (rangeStart != null) {
            predicates.add(cb.between(root.get("eventDate"), rangeStart, LocalDateTime.now()));
        } else {
            predicates.add(cb.greaterThan(root.get("eventDate"), LocalDateTime.now()));
        }
        return predicates;
    }
}
