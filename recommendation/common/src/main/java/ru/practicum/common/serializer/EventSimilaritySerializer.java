package ru.practicum.common.serializer;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public class EventSimilaritySerializer extends BaseAvroSerializer<EventSimilarityAvro> {
    public EventSimilaritySerializer() {
        super(EventSimilarityAvro.getClassSchema());
    }
}
