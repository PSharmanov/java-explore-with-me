package ru.practicum.ewm.requests.service;

import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResponse;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsByUser(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    EventRequestStatusUpdateResponse updateRequestsStatus(Long userId,
                                                          Long eventId,
                                                          EventRequestStatusUpdateRequest request);

    List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId);

}
