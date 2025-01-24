package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.interaction.model.Event;

import java.util.Collection;
import java.util.Set;


@Repository
public interface AdminEventRepository extends JpaRepository<Event, Long>, AdminEventFullDtoRepository {

    Set<Event> findAllByIdIn(Collection<Long> id);

}
