package ru.practicum.interaction.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.model.Event;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventFullDtoMapper {
    @Mapping(target = "confirmedRequests", source = "confirmedRequests")
    @Mapping(target = "views", source = "views")
    @Mapping(target = "initiator.id", source = "event.initiatorId")
    @Mapping(target = "initiator.name", source = "event.initiatorName")
    EventFullDto toDto(Event event, Long confirmedRequests, Long views);

    @Mapping(source = "eventFullDto.initiator.id", target = "initiatorId")
    @Mapping(source = "eventFullDto.initiator.name", target = "initiatorName")
    Event toEntity(EventFullDto eventFullDto);
}
