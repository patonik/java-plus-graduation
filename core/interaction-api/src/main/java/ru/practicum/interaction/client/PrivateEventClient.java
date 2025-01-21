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

@FeignClient(name = "event-service", path = "users/{userId}/events")
public interface PrivateEventClient {
    @GetMapping
    ResponseEntity<List<EventShortDto>> getMyEvents(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                                    @RequestParam(required = false,
                                                        defaultValue = DataTransferConvention.FROM)
                                                    Integer from,
                                                    @RequestParam(required = false,
                                                        defaultValue = DataTransferConvention.SIZE)
                                                    Integer size);
    @PostMapping
    ResponseEntity<EventFullDto> addEvent(@PathVariable("userId") @Min(1) @NotNull Long userId,
                                          @RequestBody @Valid NewEventDto newEventDto);
    @GetMapping("/{eventId}")
    ResponseEntity<EventFullDto> getMyEvent(@PathVariable @Min(1) @NotNull Long userId,
                                            @PathVariable @Min(1) @NotNull Long eventId);
    @PatchMapping("/{eventId}")
    ResponseEntity<EventFullDto> updateMyEvent(@PathVariable @Min(1) @NotNull Long userId,
                                               @PathVariable @Min(1) @NotNull Long eventId,
                                               @RequestBody @Valid
                                               UpdateEventUserRequest updateEventUserRequest);
    @GetMapping("/{eventId}/requests")
    ResponseEntity<List<ParticipationRequestDto>> getMyEventRequests(@PathVariable @Min(1) @NotNull Long userId,
                                                                     @PathVariable @Min(1) @NotNull
                                                                     Long eventId);
    @PatchMapping("/{eventId}/requests")
    ResponseEntity<EventRequestStatusUpdateResult> updateMyEventRequests(
        @PathVariable @Min(1) @NotNull Long userId,
        @PathVariable @Min(1) @NotNull Long eventId,
        @RequestBody @Valid
        EventRequestStatusUpdateRequest updateRequest);
}
