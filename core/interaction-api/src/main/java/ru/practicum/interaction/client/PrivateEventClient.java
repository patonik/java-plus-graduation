package ru.practicum.interaction.client;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.event.request.EventRequestStatusUpdateRequest;
import ru.practicum.interaction.dto.event.request.EventRequestStatusUpdateResult;
import ru.practicum.interaction.dto.event.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "event-service", contextId = "privateEventClient")
public interface PrivateEventClient {
    @GetMapping("/users/{userId}/events")
    ResponseEntity<List<EventShortDto>> getMyEvents(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                                    @RequestParam(required = false,
                                                        defaultValue = DataTransferConvention.FROM)
                                                    Integer from,
                                                    @RequestParam(required = false,
                                                        defaultValue = DataTransferConvention.SIZE)
                                                    Integer size);

    @PostMapping(value = "/users/{userId}/events", consumes = "application/json")
    ResponseEntity<EventFullDto> addEvent(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                          @RequestBody @Valid NewEventDto newEventDto);

    @GetMapping("/users/{userId}/events/{eventId}")
    ResponseEntity<EventFullDto> getMyEvent(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                            @PathVariable("eventId") @Min(1) @NotNull Long eventId);

    @PatchMapping(value = "/users/{userId}/events/{eventId}", consumes = "application/json")
    ResponseEntity<EventFullDto> updateMyEvent(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                               @PathVariable("eventId") @Min(1) @NotNull Long eventId,
                                               @RequestBody @Valid
                                               UpdateEventUserRequest updateEventUserRequest);

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getMyEventRequests(
        @PathVariable("userId") @Min(1) @NotNull Long userId,
        @PathVariable("eventId") @Min(1) @NotNull
        Long eventId);

    @PatchMapping(value = "/users/{userId}/events/{eventId}/requests", consumes = "application/json")
    ResponseEntity<EventRequestStatusUpdateResult> updateMyEventRequests(
        @PathVariable("userId") @Min(1) @NotNull Long userId,
        @PathVariable("eventId") @Min(1) @NotNull Long eventId,
        @RequestBody @Valid
        EventRequestStatusUpdateRequest updateRequest);
}
