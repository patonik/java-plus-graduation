package ru.practicum.interaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.interaction.dto.event.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.event.request.Status;
import ru.practicum.interaction.model.Request;

import java.util.List;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface RequestClient {
    @GetMapping
    ResponseEntity<List<ParticipationRequestDto>> getMyRequests(@PathVariable Long userId);

    @PostMapping
    ResponseEntity<ParticipationRequestDto> addMyRequest(@PathVariable Long userId, @RequestParam Long eventId);

    @PatchMapping("/{requestId}/cancel")
    ResponseEntity<ParticipationRequestDto> cancelMyRequest(@PathVariable Long userId,
                                                            @PathVariable Long requestId);

    @GetMapping("/{eventId}")
    ResponseEntity<List<Request>> getAllRequestsForEvent(@PathVariable Long userId, @PathVariable Long eventId);

    @PatchMapping("/update/{status}")
    ResponseEntity<Void> updateRequestsForEvent(@PathVariable Long userId,
                                                @PathVariable("status") Status requestStatus,
                                                @RequestBody List<Long> requestIds);
}
