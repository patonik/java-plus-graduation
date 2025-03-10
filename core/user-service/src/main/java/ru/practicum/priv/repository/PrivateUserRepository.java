package ru.practicum.priv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.interaction.model.User;

public interface PrivateUserRepository extends JpaRepository<User, Long> {
}
