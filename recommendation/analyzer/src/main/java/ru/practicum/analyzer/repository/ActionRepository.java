package ru.practicum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.analyzer.entity.UserAction;

@Repository
public interface ActionRepository extends JpaRepository<Long, UserAction> {
}
