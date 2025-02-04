package ru.practicum.pub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.interaction.dto.event.State;
import ru.practicum.interaction.dto.event.request.RequestCount;
import ru.practicum.interaction.dto.event.request.Status;
import ru.practicum.interaction.model.Event;

import java.util.Optional;

@Repository
public interface PublicEventRepository extends JpaRepository<Event, Long>, PublicEventShortDtoRepository {
    @Query("""
        select new ru.practicum.interaction.dto.event.request.RequestCount(count(r.id))
        from Request r
        where r.eventId=:eventId and r.status=:status
        """)
    RequestCount getRequestCountByEventAndStatus(Long eventId, Status status);

    Optional<Event> findByIdAndState(Long id, State state);
}