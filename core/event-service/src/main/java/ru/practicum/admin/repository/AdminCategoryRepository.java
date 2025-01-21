package ru.practicum.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.interaction.model.Category;

@Repository
public interface AdminCategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
