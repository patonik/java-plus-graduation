package ru.practicum.stats.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import ru.practicum.stats.constants.DataTransferConvention;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateTime = p.getText();
        try {
            return LocalDateTime.parse(dateTime, DataTransferConvention.DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
