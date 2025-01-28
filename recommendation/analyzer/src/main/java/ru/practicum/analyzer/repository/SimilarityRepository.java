package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.entity.EventSimilarity;

@Repository
public interface SimilarityRepository extends JpaRepository<Long, EventSimilarity> {
}
