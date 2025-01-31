package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.entity.UserAction;

import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<UserAction, Long> {
    @Query("SELECT ua.eventId AS eventId, SUM(ua.actionType) AS totalWeight " +
        "FROM UserAction ua " +
        "WHERE ua.eventId IN :eventIds " +
        "GROUP BY ua.eventId")
    List<InteractionCountResult> findInteractionCountsByEventIds(List<Long> eventIds);

    interface InteractionCountResult {
        Long getEventId();

        Double getTotalWeight();
    }

    @Query("SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId " +
        "ORDER BY ua.timestamp DESC " +
        "LIMIT :max")
    List<Long> findRecentInteractionsByUser(Long userId, long max);

}
