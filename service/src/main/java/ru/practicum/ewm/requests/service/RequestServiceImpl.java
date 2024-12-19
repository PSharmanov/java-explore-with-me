package ru.practicum.ewm.requests.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.requests.dto.EventRequestStatusUpdateResponse;
import ru.practicum.ewm.requests.dto.ParticipationRequestDto;
import ru.practicum.ewm.requests.mapper.RequestMapper;
import ru.practicum.ewm.requests.model.ParticipationRequest;
import ru.practicum.ewm.requests.model.RequestStatus;
import ru.practicum.ewm.requests.repository.RequestRepository;
import ru.practicum.ewm.user.entity.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.practicum.ewm.requests.model.RequestStatus.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private static final int NO_LIMIT = 0;
    final private EventRepository eventRepository;
    final private UserRepository userRepository;
    final private RequestRepository requestRepository;
    final private RequestMapper requestMapper;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id =" + eventId + " не найдено."));

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConflictException("Запрос уже создан.");
        }

        if (event.getInitiator().equals(user) || !event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Невозможно создать запрос.");
        }

        Integer participantLimit = event.getParticipantLimit();


        if (participantLimit != NO_LIMIT && requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= participantLimit) {
            throw new ConflictException("Превышен лимит участников.");
        }


        ParticipationRequest request = new ParticipationRequest();
        request.setEvent(event);
        request.setRequester(user);
        request.setStatus(event.getRequestModeration() && participantLimit != NO_LIMIT ? PENDING : CONFIRMED);


        ParticipationRequest newRequest = requestRepository.save(request);

        return requestMapper.toDto(newRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUser(Long userId) {

        return requestRepository.findAllByRequesterId(userId).stream()
                .map(requestMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest request = requestRepository.findByIdAndRequesterId(requestId, userId);
        request.setStatus(RequestStatus.CANCELED);
        return requestMapper.toDto(requestRepository.save(request));
    }


    @Override
    @Transactional
    public EventRequestStatusUpdateResponse updateRequestsStatus(Long userId, Long eventId,
                                                                 EventRequestStatusUpdateRequest statusUpdateRequest) {

        final RequestStatus rejectedStatus = REJECTED;
        final RequestStatus confirmedStatus = CONFIRMED;


        User initiator = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id =" + eventId + " не найдено."));

        Integer eventParticipantLimit = Objects.requireNonNullElse(event.getParticipantLimit(), 0);

        if (!event.getInitiator().equals(initiator)) {
            throw new ValidationException("Пользователь не является инициатором события.");
        }

        long confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, CONFIRMED);


        if (eventParticipantLimit > 0 && eventParticipantLimit <= confirmedRequests) {
            throw new ForbiddenException("Достигнут лимит участников события.");
        }


        List<ParticipationRequest> requests = requestRepository.findAllByEventIdAndIdInAndStatus(eventId,
                statusUpdateRequest.getRequestIds(), PENDING);

        List<ParticipationRequest> confirmedRequestsList = new ArrayList<>();
        List<ParticipationRequest> rejectedRequestsList = new ArrayList<>();


        for (ParticipationRequest request : requests) {

            if (statusUpdateRequest.getStatus() == REJECTED) {
                request.setStatus(REJECTED);
                rejectedRequestsList.add(request);
            } else if (statusUpdateRequest.getStatus() == CONFIRMED && (eventParticipantLimit == 0 || confirmedRequests < eventParticipantLimit)) {
                request.setStatus(CONFIRMED);
                confirmedRequestsList.add(request);
                confirmedRequests++;
            } else {
                request.setStatus(REJECTED);
                rejectedRequestsList.add(request);
            }

        }
        requestRepository.saveAll(requests);

        List<ParticipationRequestDto> confirmed = confirmedRequestsList.stream().map(requestMapper::toDto).collect(Collectors.toList());
        List<ParticipationRequestDto> rejected = rejectedRequestsList.stream().map(requestMapper::toDto).collect(Collectors.toList());


        return new EventRequestStatusUpdateResponse(confirmed, rejected);
    }


    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByEventOwner(Long userId, Long eventId) {

        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("Событие с id =" + eventId + " не найдено.");
        }


        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .collect(Collectors
                        .toList());
    }


}




