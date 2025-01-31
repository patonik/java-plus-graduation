package ru.practicum.analyzer.entity;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventSimilarityMapper {
    EventSimilarity toEntity(EventSimilarityAvro eventSimilarityAvro);

    EventSimilarityAvro toAvro(EventSimilarity eventSimilarity);
}
