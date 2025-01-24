package ru.practicum.priv.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.client.RequestClient;
import ru.practicum.interaction.client.UserClient;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventFullDtoMapper;
import ru.practicum.interaction.dto.event.EventShortDto;
import ru.practicum.interaction.dto.event.NewEventDto;
import ru.practicum.interaction.dto.event.NewEventDtoMapper;
import ru.practicum.interaction.dto.event.State;
import ru.practicum.interaction.dto.event.UpdateEventUserRequest;
import ru.practicum.interaction.dto.event.UpdateEventUserRequestMapper;
import ru.practicum.interaction.dto.event.UserStateAction;
import ru.practicum.interaction.dto.event.request.*;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserDtoMapper;
import ru.practicum.stats.HttpStatsClient;
import ru.practicum.stats.dto.StatResponseDto;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Category;
import ru.practicum.interaction.model.Event;
import ru.practicum.interaction.model.Request;
import ru.practicum.interaction.model.User;
import ru.practicum.priv.repository.PrivateCategoryRepository;
import ru.practicum.priv.repository.PrivateEventRepository;
import ru.practicum.interaction.util.StatParams;
import ru.practicum.interaction.util.Statistical;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrivateEventService {
    private final PrivateEventRepository privateEventRepository;
    private final PrivateCategoryRepository privateCategoryRepository;
    private final NewEventDtoMapper newEventDtoMapper;
    private final EventFullDtoMapper eventFullDtoMapper;
    private final UpdateEventUserRequestMapper updateEventUserRequestMapper;
    private final HttpStatsClient httpStatsClient;
    private final RequestClient requestClient;
    private final RequestDtoMapper requestDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserClient userClient;

    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        List<UserDto> userDtos;
        try {
            userDtos = userClient.getUsers(List.of(userId), 0, 1).getBody();
            if (userDtos == null || userDtos.isEmpty()) {
                throw new RuntimeException("Not authorized to add new event");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e.getMessage());
        }
        User initiator = userDtoMapper.toUser(userDtos.getFirst());
        Category category = privateCategoryRepository.findById(newEventDto.getCategoryId())
            .orElseThrow(() -> new RuntimeException("No such category"));
        Event event = newEventDtoMapper.toEvent(newEventDto, initiator, category, State.PENDING);
        event = privateEventRepository.save(event);
        Long confirmedRequests = 0L;
        Long views = 0L;
        return eventFullDtoMapper.toDto(event, confirmedRequests, views);
    }

    @Transactional(readOnly = true)
    public List<EventShortDto> getMyEvents(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        List<EventShortDto> eventShortDtos = new ArrayList<>(privateEventRepository.getEvents(userId, pageable));
        log.info("found {} events", eventShortDtos.size());
        StatParams statParams = Statistical.getParams(new ArrayList<>(eventShortDtos));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDto =
            httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Map<Long, Long> hitMap = statResponseDto
            .stream()
            .collect(Collectors.toMap(x -> Long.parseLong(x.getUri().split("/")[2]), StatResponseDto::getHits));
        eventShortDtos.sort(Comparator.comparingLong(EventShortDto::getId));
        if (hitMap.isEmpty()) {
            return eventShortDtos;
        }
        for (EventShortDto eventShortDto : eventShortDtos) {
            RequestCount requestCount = null;
            try {
                requestCount = requestClient.getAllConfirmedRequestsForEvent(eventShortDto.getInitiator().getId(), eventShortDto.getId()).getBody();
                if (requestCount == null) {
                    throw new RuntimeException("Error trying to count requests");
                }
            } catch (FeignException.NotFound e) {
                throw new RuntimeException(e);
            }
            eventShortDto.setConfirmedRequests(requestCount.getConfirmedRequests());
            Long eventId = eventShortDto.getId();
            eventShortDto.setViews(hitMap.getOrDefault(eventId, 0L));
        }
        log.info("found {} eventShortDtos", eventShortDtos.size());
        return eventShortDtos;
    }

    @Transactional(readOnly = true)
    public EventFullDto getMyEvent(Long userId, Long eventId) {
        Event event =
            privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        RequestCount requestCount = null;
        try {
            requestCount = requestClient.getAllConfirmedRequestsForEvent(event.getInitiatorId(), eventId).getBody();
            if (requestCount == null) {
                throw new RuntimeException("Error trying to count requests");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e);
        }
        Long confirmedRequests = requestCount.getConfirmedRequests();
        EventFullDto eventFullDto = eventFullDtoMapper.toDto(event, confirmedRequests, 0L);
        StatParams statParams = Statistical.getParams(List.of(eventFullDto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDtos =
            httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        if (!statResponseDtos.isEmpty()) {
            eventFullDto.setViews(statResponseDtos.getFirst().getHits());
        }
        return eventFullDto;
    }

    @Transactional
    public EventFullDto updateMyEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event =
            privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("cannot modify in current state");
        }
        final Long categoryId = updateEventUserRequest.getCategoryId();
        Category category;
        if (categoryId == null) {
            category = event.getCategory();
        } else {
            category = privateCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
        }
        event = updateEventUserRequestMapper.updateEvent(updateEventUserRequest, event, category);
        UserStateAction userStateAction = updateEventUserRequest.getUserStateAction();
        log.info("userStateAction value: {}", userStateAction);
        switch (userStateAction) {
            case null:
                break;
            case UserStateAction.SEND_TO_REVIEW:
                event.setState(State.PENDING);
                log.info("event state updated to {}", event.getState());
                break;
            case UserStateAction.CANCEL_REVIEW:
                event.setState(State.CANCELED);
                break;
        }
        event = privateEventRepository.save(event);
        RequestCount requestCount = null;
        try {
            requestCount = requestClient.getAllConfirmedRequestsForEvent(event.getInitiatorId(), eventId).getBody();
            if (requestCount == null) {
                throw new RuntimeException("Error trying to count requests");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e);
        }
        Long confirmedRequests = requestCount.getConfirmedRequests();
        EventFullDto dto = eventFullDtoMapper.toDto(event, confirmedRequests, 0L);
        StatParams statParams = Statistical.getParams(List.of(dto));
        log.info("parameters for statService created: {}", statParams);
        List<StatResponseDto> statResponseDto =
            httpStatsClient.getStats(statParams.start(), statParams.end(), statParams.uriList(), true);
        Long hits = 0L;
        if (!statResponseDto.isEmpty()) {
            hits = statResponseDto.getFirst().getHits();
        }
        dto.setViews(hits);
        log.info("eventFullDto state returned: {}", dto.getState());
        return dto;
    }

    public List<ParticipationRequestDto> getMyEventRequests(Long userId, Long eventId) {
        boolean exists =
            privateEventRepository.existsByIdAndInitiatorId(eventId, userId);
        if (!exists) {
            throw new NotFoundException("Event not found: " + eventId);
        }
        List<Request> requests;
        try {
            requests = requestClient.getAllRequestsForEvent(userId, eventId).getBody();
            if (requests == null) {
                throw new RuntimeException("call for RequestDtos failed");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e.getMessage());
        }
        return new ArrayList<>(requestDtoMapper.toParticipationRequestDtos(requests));
    }

    @Transactional
    public EventRequestStatusUpdateResult updateMyEventRequests(Long userId, Long eventId,
                                                                EventRequestStatusUpdateRequest updateRequest) {
        Event event =
            privateEventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found: " + eventId));


        return verifyAndUpdate(updateRequest.getRequestIds(),
            updateRequest.getStatus(), event);
    }

    private EventRequestStatusUpdateResult verifyAndUpdate(Set<Long> updateRequestIds,
                                                           Status updateRequestStatus,
                                                           Event event) {
        Long eventId = event.getId();
        Integer participantLimit = event.getParticipantLimit();
        Long initiatorId = event.getInitiatorId();
        if (participantLimit.equals(0) || event.getRequestModeration().equals(false)) {
            return new EventRequestStatusUpdateResult();
        }
        List<Request> allRequests;
        try {
            allRequests = requestClient.getAllRequestsForEvent(initiatorId, eventId).getBody();
            if (allRequests == null) {
                throw new RuntimeException("requests not found");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e.getMessage());
        }
        log.info("found all requests for event {}", allRequests);
        Map<Status, Map<Long, Request>> statusSetMap = allRequests.stream()
            .collect(Collectors.groupingBy(Request::getStatus, Collectors.toMap(Request::getId, Function.identity())));
        Map<Long, Request> pendingRequests = statusSetMap.get(Status.PENDING);
        log.info("pending requests are: {}", pendingRequests);
        if (pendingRequests == null || pendingRequests.isEmpty()) {
            throw new ConflictException("No pending requests for: " + eventId);
        }
        Set<Long> pendingRequestIds = pendingRequests.keySet();
        if (!pendingRequestIds.containsAll(updateRequestIds)) {
            throw new ConflictException("Cannot update, status not pending: " + eventId);
        }
        Set<ParticipationRequestDto> confirmed;
        Set<ParticipationRequestDto> rejected;
        switch (updateRequestStatus) {
            case Status.CONFIRMED:
                Set<Long> confirmedRequests = statusSetMap.getOrDefault(Status.CONFIRMED, Map.of()).keySet();
                int confirmedSize = confirmedRequests.size();
                int total = updateRequestIds.size() + confirmedSize;
                if (total > participantLimit) {
                    throw new ConflictException("Requests limit exceeded");
                }
                requestClient.updateRequestsForEvent(initiatorId, updateRequestStatus,
                    new ArrayList<>(updateRequestIds));
                Map<Long, Request> confirmedCopy = new HashMap<>(pendingRequests);
                confirmedCopy.keySet().retainAll(updateRequestIds);
                Collection<Request> confirmedValues = confirmedCopy.values();
                confirmedValues.forEach(x -> x.setStatus(Status.CONFIRMED));
                confirmed =
                    requestDtoMapper.toParticipationRequestDtos(confirmedValues);
                rejected = Set.of();
                if (total == participantLimit) {
                    pendingRequestIds.removeAll(updateRequestIds);
                    requestClient.updateRequestsForEvent(initiatorId, Status.REJECTED,
                        new ArrayList<>(pendingRequestIds));
                    Collection<Request> pendingValues = pendingRequests.values();
                    pendingValues.forEach(x -> x.setStatus(Status.REJECTED));
                    rejected = requestDtoMapper.toParticipationRequestDtos(pendingValues);
                }
                return new EventRequestStatusUpdateResult(confirmed, rejected);
            case Status.REJECTED:
                requestClient.updateRequestsForEvent(initiatorId, updateRequestStatus,
                    new ArrayList<>(updateRequestIds));
                Collection<Request> pendingValues = pendingRequests.values();
                pendingValues.forEach(x -> x.setStatus(Status.REJECTED));
                rejected =
                    requestDtoMapper.toParticipationRequestDtos(pendingValues);
                confirmed = Set.of();
                return new EventRequestStatusUpdateResult(confirmed, rejected);
            case null, default:
                throw new ConflictException("Incorrect update request status: " + updateRequestStatus);
        }
    }
}
