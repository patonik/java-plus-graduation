package ru.practicum.interaction.dto.compilation;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.model.Compilation;
import ru.practicum.interaction.model.Event;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompilationDtoMapper {
    CompilationDto toCompilationDto(Compilation compilation);

    @Mapping(target = "initiator.id", source = "event.initiatorId")
    @Mapping(target = "initiator.name", source = "event.initiatorName")
    EventShortDto toEventShortDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", source = "events")
    Compilation updateCompilation(UpdateCompilationRequest updateCompilationRequest,
                                  @MappingTarget Compilation compilation, Set<Event> events);

    List<CompilationDto> toCompilationDtoList(List<Compilation> compilations);

    List<EventShortDto> toEventShortDtoList(List<Event> events);
}
