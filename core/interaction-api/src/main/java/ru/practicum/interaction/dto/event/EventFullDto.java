package ru.practicum.interaction.dto.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.user.UserShortDto;
import ru.practicum.stats.json.LocalDateTimeDeserializer;
import ru.practicum.stats.json.LocalDateTimeSerializer;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventFullDto implements EventDto {
    private Long id;
    private String annotation;
    private CategoryDto category;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdOn;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime publishedOn;
    private String description;
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Location location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private String title;
    private State state;
    private Long confirmedRequests;
    private Long views;
}
