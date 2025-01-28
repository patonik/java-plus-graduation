package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.entity.EventSimilarity;

import java.util.List;

@Repository
public interface SimilarityRepository extends JpaRepository<EventSimilarity, Long> {
    @Query("SELECT es.eventIdB AS similarEventId, es.score AS similarityScore " +
        "FROM EventSimilarity es " +
        "WHERE es.eventIdA = :eventId " +
        "AND es.eventIdB NOT IN (SELECT ua.eventId FROM UserAction ua WHERE ua.userId = :userId) " +
        "ORDER BY es.score DESC")
    List<SimilarEventResult> findSimilarEventsExcludingUserInteractions(Long eventId, Long userId);

    interface SimilarEventResult {
        Long getSimilarEventId();

        Double getSimilarityScore();
    }

    @Query("SELECT es.eventIdB AS similarEvent, es.score AS similarityScore " +
        "FROM EventSimilarity es " +
        "WHERE es.eventIdA = :eventId " +
        "AND es.score > 0 " +
        "ORDER BY es.score DESC")
    List<SimilarEventResult> findSimilarEventsForEvent(Long eventId);

}
