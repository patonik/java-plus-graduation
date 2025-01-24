package ru.practicum.interaction.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.model.Category;
import ru.practicum.interaction.model.Event;
import ru.practicum.interaction.model.User;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface NewEventDtoMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "initiatorId", source = "user.id")
    @Mapping(target = "initiatorName", source = "user.name")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "category", source = "category")
    Event toEvent(NewEventDto newEventDto, User user, Category category, State state);
}
