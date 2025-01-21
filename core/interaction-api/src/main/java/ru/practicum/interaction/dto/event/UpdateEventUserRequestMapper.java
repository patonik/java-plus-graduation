package ru.practicum.interaction.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.interaction.model.Category;
import ru.practicum.interaction.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UpdateEventUserRequestMapper {
    @Mapping(target = "category", source = "category")
    @Mapping(target = "id", ignore = true)
    Event updateEvent(UpdateEventUserRequest updateEventUserRequest, @MappingTarget Event event, Category category);
}
