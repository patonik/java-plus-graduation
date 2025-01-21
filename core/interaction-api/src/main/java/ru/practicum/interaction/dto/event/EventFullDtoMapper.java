package ru.practicum.interaction.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventFullDtoMapper {
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    EventFullDto toDto(Event event, Long confirmedRequests, Long views);

    Event toEntity(EventFullDto eventFullDto);
}
