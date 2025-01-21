package ru.practicum.priv.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.client.PublicEventClient;
import ru.practicum.interaction.client.UserClient;
import ru.practicum.interaction.dto.event.EventFullDto;
import ru.practicum.interaction.dto.event.EventFullDtoMapper;
import ru.practicum.interaction.dto.event.State;
import ru.practicum.interaction.dto.event.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.event.request.RequestDtoMapper;
import ru.practicum.interaction.dto.event.request.Status;
import ru.practicum.interaction.dto.user.UserDto;
import ru.practicum.interaction.dto.user.UserDtoMapper;
import ru.practicum.interaction.exception.ConflictException;
import ru.practicum.interaction.exception.NotFoundException;
import ru.practicum.interaction.model.Event;
import ru.practicum.interaction.model.Request;
import ru.practicum.interaction.model.User;
import ru.practicum.priv.repository.RequestRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PrivateRequestServiceImpl implements PrivateRequestService {
    private final RequestRepository requestRepository;
    private final RequestDtoMapper requestMapper;
    private final UserDtoMapper userDtoMapper;
    private final UserClient userClient;
    private final PublicEventClient publicEventClient;
    private final EventFullDtoMapper eventFullDtoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getMyRequests(Long userId) {
        return requestRepository.findDtosByRequesterId(userId);
    }

    @Override
    public ParticipationRequestDto addMyRequest(Long userId, Long eventId) {
        List<UserDto> userDtos;
        try {
            userDtos = userClient.getUsers(List.of(userId), 0, 1).getBody();
            if (userDtos == null || userDtos.isEmpty()) {
                throw new RuntimeException("User not found");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e.getMessage());
        }
        User user = userDtoMapper.toUser(userDtos.getFirst());
        EventFullDto eventFullDto;
        try {
            eventFullDto = publicEventClient.getEvent(eventId).getBody();
            if (eventFullDto == null) {
                throw new RuntimeException("Event not found");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException(e.getMessage());
        }
        Event event = eventFullDtoMapper.toEntity(eventFullDto);
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("""
                the initiator of the event cannot add a participation request for their own event.
                """);
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("cannot participate in an unpublished event.");
        }
        if (requestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("request already exists");
        }
        long confirmedAmount = requestRepository.countByEventIdAndStatus(eventId, Status.CONFIRMED);
        int participantLimit = event.getParticipantLimit();
        if (participantLimit > 0 && confirmedAmount >= participantLimit) {
            throw new ConflictException("participant limit exceeded");
        }
        Status status = Status.PENDING;
        if (!event.getRequestModeration() || participantLimit == 0) {
            status = Status.CONFIRMED;
        }
        var request = new Request(null, LocalDateTime.now(), user, event, status);

        return requestMapper.toParticipationRequestDto(requestRepository.save(request));
    }

    @Override
    public ParticipationRequestDto cancelMyRequest(Long userId, Long requestId) {
        var request = requestRepository.findByIdAndRequesterId(requestId, userId).orElseThrow(
            () -> new NotFoundException(String.format("request with id %s", requestId)));
        request.setStatus(Status.CANCELED);
        return requestMapper.toParticipationRequestDto(request);
    }

    @Override
    public List<Request> getAllRequestsForEvent(Long userId, Long eventId) {
        return requestRepository.findAllByEventId(eventId);
    }

    @Override
    public void updateRequestsForEvent(Long userId, Status requestStatus, List<Long> requestIds) {
        requestRepository.updateAllByIds(new HashSet<>(requestIds), requestStatus);
    }
}
