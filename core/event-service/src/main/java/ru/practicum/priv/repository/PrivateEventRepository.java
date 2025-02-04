package ru.practicum.priv.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.interaction.model.Event;

import java.util.List;
import java.util.Optional;


@Repository
public interface PrivateEventRepository extends JpaRepository<Event, Long>, PrivateEventShortDtoRepository {


    List<Event> findAllByInitiatorIdOrderByCreatedOnAsc(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long id, Long initiatorId);

    boolean existsByIdAndInitiatorId(Long id, Long initiatorId);
}
