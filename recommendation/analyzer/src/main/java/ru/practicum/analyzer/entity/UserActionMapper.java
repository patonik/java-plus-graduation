package ru.practicum.analyzer.entity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserActionMapper {
    UserAction toEntity(UserActionAvro userActionAvro);

    UserActionAvro toAvro(UserAction userAction);
}
