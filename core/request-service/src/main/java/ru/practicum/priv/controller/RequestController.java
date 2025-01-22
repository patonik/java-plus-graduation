package ru.practicum.priv.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.interaction.client.RequestClient;
import ru.practicum.interaction.dto.event.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.event.request.Status;
import ru.practicum.interaction.model.Request;
import ru.practicum.priv.service.PrivateRequestServiceImpl;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class RequestController implements RequestClient {

    private final PrivateRequestServiceImpl requestService;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getMyRequests(@PathVariable Long userId) {
        log.atInfo()
            .addArgument(userId)
            .log("Received request to get participation requests for userId: {}");
        return new ResponseEntity<>(requestService.getMyRequests(userId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addMyRequest(@PathVariable Long userId, @RequestParam Long eventId) {
        log.atInfo()
            .addArgument(userId)
            .addArgument(eventId)
            .log("Received request to add participation request with userId: {} and eventId: {}");
        return new ResponseEntity<>(requestService.addMyRequest(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelMyRequest(@PathVariable Long userId,
                                                                   @PathVariable Long requestId) {
        log.atInfo()
            .addArgument(userId)
            .addArgument(requestId)
            .log("Received request to cancel participation request with userId: {} and requestId: {}");
        return new ResponseEntity<>(requestService.cancelMyRequest(userId, requestId), HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<List<Request>> getAllRequestsForEvent(@PathVariable Long userId, @PathVariable Long eventId) {
        log.atInfo()
            .addArgument(userId)
            .addArgument(eventId)
            .log("Received request to get participation request with userId: {} and eventId: {}");
        return new ResponseEntity<>(
            requestService.getAllRequestsForEvent(userId, eventId),
            HttpStatus.OK);
    }

    @PostMapping("/update/{status}")
    public ResponseEntity<Void> updateRequestsForEvent(@PathVariable Long userId,
                                                       @PathVariable("status") Status requestStatus,
                                                       @RequestBody List<Long> requestIds) {
        log.atInfo()
            .addArgument(userId)
            .addArgument(requestStatus)
            .log("Received request to update participation requests with userId: {} to requestStatus: {}");
        requestService.updateRequestsForEvent(userId, requestStatus, requestIds);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
