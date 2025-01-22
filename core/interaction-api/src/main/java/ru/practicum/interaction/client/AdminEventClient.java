package ru.practicum.interaction.client;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.State;
import ru.practicum.interaction.dto.event.UpdateEventAdminRequest;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service", path = "/admin/events", contextId = "adminEventClient")
public interface AdminEventClient {
    @GetMapping
    ResponseEntity<List<EventFullDto>> getEvents(@RequestParam(required = false) List<Long> users,
                                                 @RequestParam(required = false) List<State> states,
                                                 @RequestParam(required = false) List<Long> categories,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeStart,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                 LocalDateTime rangeEnd,
                                                 @RequestParam(required = false) List<Long> loci,
                                                 @RequestParam(required = false, defaultValue = "0")
                                                 Integer from,
                                                 @RequestParam(required = false, defaultValue = "10")
                                                 Integer size);

    @PatchMapping(value="/{eventId}", consumes = "application/json")
    ResponseEntity<EventFullDto> updateEvent(@PathVariable Long eventId,
                                             @RequestBody @Valid UpdateEventAdminRequest adminRequest);
}
