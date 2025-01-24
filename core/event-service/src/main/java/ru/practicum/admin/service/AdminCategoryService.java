package ru.practicum.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.admin.repository.AdminCategoryRepository;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Category;


@Service
@RequiredArgsConstructor
public class AdminCategoryService {
    private final AdminCategoryRepository adminCategoryRepository;

    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        String name = newCategoryDto.getName();
        if (adminCategoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name " + name + " already exists");
        }
        Category category = new Category();
        category.setName(name);
        category = adminCategoryRepository.save(category);
        return new CategoryDto(category.getId(), category.getName());
    }

    public void deleteCategory(Long catId) {
        if (!adminCategoryRepository.existsById(catId)) {
            throw new NotFoundException("Category with id " + catId + " does not exist");
        }
        adminCategoryRepository.deleteById(catId);
    }

    public CategoryDto updateCategory(Long catId, NewCategoryDto newCategoryDto) {
        Category found = adminCategoryRepository.findById(catId)
            .orElseThrow(() -> new NotFoundException("Category with id " + catId + " does not exist"));
        String name = newCategoryDto.getName();
        if (!found.getName().equals(name) && adminCategoryRepository.existsByName(name)) {
            throw new ConflictException("Category with name " + name + " already exists");
        }
        found.setName(name);
        Category saved = adminCategoryRepository.save(found);
        return new CategoryDto(saved.getId(), saved.getName());
    }

}
