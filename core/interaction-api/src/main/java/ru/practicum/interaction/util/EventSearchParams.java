package ru.practicum.interaction.util;

import ru.practicum.interaction.dto.event.State;

import java.time.LocalDateTime;
import java.util.List;

public record EventSearchParams(List<Long> users,
                                List<State> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                List<Long> loci,
                                Integer from,
                                Integer size) {
}
