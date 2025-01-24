package ru.practicum.interaction.dto.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.model.Event;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventShortDtoMapper {
    @Mapping(target = "views", source = "hits")
    @Mapping(target = "initiator.id", source = "event.initiatorId")
    @Mapping(target = "initiator.name", source = "event.initiatorName")
    EventShortDto toDto(Event event, long hits);

    @Mapping(target = "views", ignore = true)
    List<EventShortDto> toDtos(List<Event> events);
}
