package ru.practicum.interaction.dto.event.request;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.interaction.model.Request;

import java.util.Collection;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface RequestDtoMapper {
    ParticipationRequestDto toParticipationRequestDto(Request request);

    Set<ParticipationRequestDto> toParticipationRequestDtos(Collection<Request> requests);
}
