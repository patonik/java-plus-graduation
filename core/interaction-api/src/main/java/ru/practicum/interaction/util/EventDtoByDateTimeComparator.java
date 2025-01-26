package ru.practicum.interaction.util;

import ru.practicum.interaction.dto.event.EventDto;

import java.util.Comparator;

public class EventDtoByDateTimeComparator implements Comparator<EventDto> {
    @Override
    public int compare(EventDto x, EventDto y) {
        return x.getCreatedOn().isBefore(y.getCreatedOn()) ? -1 :
                x.getCreatedOn().isAfter(y.getCreatedOn()) ? 1 : 0;
    }
}
