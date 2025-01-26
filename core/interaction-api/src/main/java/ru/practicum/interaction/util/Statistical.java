package ru.practicum.interaction.util;

import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.interaction.dto.event.EventDto;

import java.time.LocalDateTime;
import java.util.List;

public interface Statistical {

    static StatParams getParams(List<EventDto> events) {
        String end = LocalDateTime.now().format(DataTransferConvention.DATE_TIME_FORMATTER);
        String start = events.stream().min(new EventDtoByDateTimeComparator())
                .orElseThrow(() -> new RuntimeException("start date cannot be null")).getCreatedOn()
                .format(DataTransferConvention.DATE_TIME_FORMATTER);
        List<String> uriList = events.stream().map(x -> "/events/" + x.getId()).toList();
        return new StatParams(start, end, uriList);
    }
}
