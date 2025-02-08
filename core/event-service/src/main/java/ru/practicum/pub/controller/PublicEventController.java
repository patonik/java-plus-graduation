package ru.practicum.pub.controller;

import collector.UserAction;
import com.google.protobuf.Timestamp;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.analyzer.grpc.RecommendationsProto;
import ru.practicum.grpc.EventServiceGrpcClients;
import ru.practicum.interaction.DataTransferConvention;
import ru.practicum.interaction.client.PublicEventClient;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.SortCriterium;
import ru.practicum.pub.service.PublicEventService;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class PublicEventController implements PublicEventClient {
    private final PublicEventService publicEventService;
    private final EventServiceGrpcClients grpcClients;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEvents(@RequestParam(required = false) String text,
                                                         @RequestParam(required = false) Long[] categories,
                                                         @RequestParam(required = false) Boolean paid,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                         LocalDateTime rangeStart,
                                                         @RequestParam(required = false)
                                                         @DateTimeFormat(pattern = DataTransferConvention.DATE_TIME_PATTERN)
                                                         LocalDateTime rangeEnd,
                                                         @RequestParam(required = false, defaultValue = "false")
                                                         Boolean onlyAvailable,
                                                         @RequestParam(required = false) SortCriterium sort,
                                                         @RequestParam(required = false,
                                                             defaultValue = DataTransferConvention.FROM)
                                                         Integer from,
                                                         @RequestParam(required = false,
                                                             defaultValue = DataTransferConvention.SIZE)
                                                         Integer size, HttpServletRequest request) {
        return new ResponseEntity<>(
            publicEventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size,
                request),
            HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id, HttpServletRequest request) {
        return new ResponseEntity<>(publicEventService.getEvent(id, request), HttpStatus.OK);
    }

    @GetMapping("/internal/{id}")
    public ResponseEntity<EventFullDto> getEvent(@PathVariable Long id) {
        log.info("Get event with id {}", id);
        return new ResponseEntity<>(publicEventService.getInternalEvent(id), HttpStatus.OK);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationsProto.RecommendedEventProto>> getRecommendations(
        @RequestHeader("X-EWM-USER-ID") long userId) {

        Iterator<RecommendationsProto.RecommendedEventProto> recommendations =
            grpcClients.getRecommendationsForUser(userId, 10);

        List<RecommendationsProto.RecommendedEventProto> result = StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(recommendations, Spliterator.ORDERED), false)
            .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{eventId}/like")
    public ResponseEntity<Void> likeEvent(@RequestHeader("X-EWM-USER-ID") long userId,
                                          @PathVariable long eventId) {
        // Send like action to Collector
        long millis = System.currentTimeMillis();
        grpcClients.sendUserAction(UserAction.UserActionProto.newBuilder()
            .setUserId(userId)
            .setEventId(eventId)
            .setActionType(UserAction.ActionTypeProto.ACTION_LIKE)
            .setTimestamp(Timestamp.newBuilder().setSeconds(millis / 1000)
                .setNanos((int) ((millis % 1000) * 1000000)).build())
            .build());
        return ResponseEntity.ok().build();
    }
}
