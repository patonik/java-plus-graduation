package ru.practicum.priv.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Pageable;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.interaction.model.Category;
import ru.practicum.interaction.model.Event;

import java.util.ArrayList;
import java.util.List;

public class PrivateEventShortDtoRepositoryImpl implements PrivateEventShortDtoRepository {
    @PersistenceContext
    EntityManager em;

    @Override
    public List<EventShortDto> getEvents(Long userId,
                                         Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<EventShortDto> cq = cb.createQuery(EventShortDto.class);
        Root<Event> eventRoot = cq.from(Event.class);
        Join<Event, Category> categoryJoin = eventRoot.join("category");
        cq.select(cb.construct(
                EventShortDto.class,
                eventRoot.get("id"),
                eventRoot.get("annotation"),
                cb.construct(
                        CategoryDto.class, categoryJoin.get("id"), categoryJoin.get("name")
                ),
                cb.nullLiteral(Long.class),
                eventRoot.get("eventDate"),
                eventRoot.get("createdOn"),
                cb.construct(
                        UserShortDto.class, eventRoot.get("initiatorId"), eventRoot.get("initiatorName")
                ),
                eventRoot.get("paid"),
                eventRoot.get("title"),
                cb.nullLiteral(Long.class) // views will be filled later
        ));

        List<Predicate> predicates = getPredicates(cb, eventRoot, userId);
        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        int pageSize = pageable.getPageSize();
        return em.createQuery(cq)
                .setFirstResult(pageable.getPageNumber() * pageSize)
                .setMaxResults(pageSize)
                .getResultList();
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb, Root<Event> eventRoot, Object... args) {
        List<Predicate> predicates = new ArrayList<>();
        Long userId = (Long) args[0];
        if (userId != null) {
            predicates.add(cb.equal(eventRoot.get("initiatorId"), userId));
        }
        return predicates;
    }
}
