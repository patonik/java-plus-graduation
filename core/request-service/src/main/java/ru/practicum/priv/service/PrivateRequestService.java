package ru.practicum.priv.service;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.interaction.dto.event.request.ParticipationRequestDto;
import ru.practicum.interaction.dto.event.request.RequestCount;
import ru.practicum.interaction.dto.event.request.Status;
import ru.practicum.interaction.model.Request;

import java.util.List;

public interface PrivateRequestService {

    List<ParticipationRequestDto> getMyRequests(Long userId);

    ParticipationRequestDto addMyRequest(Long userId, Long eventId);

    ParticipationRequestDto cancelMyRequest(Long userId, Long requestId);

    List<Request> getAllRequestsForEvent(@PathVariable Long userId, @PathVariable Long eventId);

    void updateRequestsForEvent(@PathVariable Long userId,
                                @PathVariable("status") Status requestStatus,
                                @RequestBody List<Long> requestIds);

    RequestCount getAllConfirmedRequestsForEvent(Long userId, Long eventId);
}
