package ru.practicum.interaction.dto.event;

import java.time.LocalDateTime;

public interface EventDto {
    LocalDateTime getCreatedOn();

    Long getId();

    void setViews(Long views);
}
