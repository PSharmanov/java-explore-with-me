package ru.practicum.ewm.events.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.ewm.StatsClient;
import ru.practicum.ewm.categories.entity.Category;
import ru.practicum.ewm.categories.repository.CategoryRepository;
import ru.practicum.ewm.events.dto.*;
import ru.practicum.ewm.events.mapper.EventMapper;
import ru.practicum.ewm.events.model.Event;
import ru.practicum.ewm.events.model.State;
import ru.practicum.ewm.events.model.StateAction;
import ru.practicum.ewm.events.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;
import ru.practicum.ewm.locations.Location;
import ru.practicum.ewm.locations.LocationMapper;
import ru.practicum.ewm.locations.LocationRepository;
import ru.practicum.ewm.requests.dto.ConfirmedRequests;
import ru.practicum.ewm.requests.repository.RequestRepository;
import ru.practicum.ewm.user.entity.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.ewm.events.model.State.PENDING;
import static ru.practicum.ewm.events.model.State.PUBLISHED;
import static ru.practicum.ewm.requests.model.RequestStatus.CONFIRMED;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final LocationMapper locationMapper;
    private final LocationRepository locationRepository;
    private final EventMapper eventMapper;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    @Value("ewm.service.name")
    String app;


    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {

        User initiator = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден."));

        Long categoryId = newEventDto.getCategory();

        Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                new NotFoundException("Категория с id = " + categoryId + " не найдена."));

        Location location = checkLocation(locationMapper.toLocation(newEventDto.getLocation()));

        Event newEvent = eventMapper.toEntity(newEventDto, initiator, category, location);

        newEvent.setCreatedOn(LocalDateTime.now());

        newEvent.setState(PENDING);

        Event saveEvent = eventRepository.save(newEvent);

        return eventMapper.toDto(saveEvent);


    }

    private Location checkLocation(Location location) {
        if (locationRepository.existsByLatAndLon(location.getLat(), location.getLon())) {
            return locationRepository.findByLatAndLon(location.getLat(), location.getLon());
        } else {
            return locationRepository.save(location);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = PageRequest.of(from / size, size, sortById);

        Page<Event> eventPage = eventRepository.findByInitiatorId(userId, page);

        if (eventPage.isEmpty()) {
            return List.of();
        }

        return eventPage.getContent().stream()
                .map(eventMapper::toShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByOwner(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));
        return eventMapper.toDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));

        updateEvent(event, updateEvent);

        return eventMapper.toDto(eventRepository.save(event));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDtoWithViews> getEventsByAdminParams(List<Long> users,
                                                              List<String> states,
                                                              List<Long> categories,
                                                              LocalDateTime rangeStart,
                                                              LocalDateTime rangeEnd,
                                                              Integer from,
                                                              Integer size) {
        Sort sortById = Sort.by(Sort.Direction.ASC, "id");

        Pageable page = PageRequest.of(from / size, size, sortById);

        Page<Event> events = eventRepository.findEventsByAdmin(users, states, categories, rangeStart, rangeEnd, page);

        return events.getContent().stream()
                .map(event -> {
                    EventFullDtoWithViews dto = eventMapper.toDtoWithView(event, 0L);
                    dto.setConfirmedRequests(requestRepository.countConfirmedRequestsByEventId(event.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public List<EventShortDtoWithViews> getEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new ValidationException("START can't ba after END.");
        }

        Specification<Event> specification = Specification.where(null);
        if (text != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), "%" + text.toLowerCase() + "%"),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + text.toLowerCase() + "%")
                    ));
        }

        if (categories != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("category").get("id").in(categories));
        }

        if (paid != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("paid"), paid));
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = Objects.requireNonNullElse(rangeStart, now);
        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(root.get("eventDate"), startDateTime));

        if (rangeEnd != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThan(root.get("eventDate"), rangeEnd));
        }

        if (onlyAvailable != null && onlyAvailable) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("participantLimit"), 0));
        }

        specification = specification.and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("state"), PUBLISHED));
        PageRequest pageRequest = switch (sort) {
            case "EVENT_DATE" -> PageRequest.of(from / size, size, Sort.by("eventDate"));
            case "VIEWS" -> PageRequest.of(from / size, size, Sort.by("views").descending());
            default -> throw new ValidationException("Неизвестный параметр сортировки: " + sort);
        };

        List<Event> events = eventRepository.findAll(specification, pageRequest).getContent();
        List<EventShortDtoWithViews> result = new ArrayList<>();

        if (!events.isEmpty()) {

            List<String> uris = events.stream()
                    .map(event -> String.format("/events/%s", event.getId()))
                    .collect(Collectors.toList());

            Optional<LocalDateTime> start = events.stream()
                    .map(Event::getCreatedOn)
                    .min(LocalDateTime::compareTo);

            ResponseEntity<Object> response = statsClient.getStats(start.get(), LocalDateTime.now(), uris, true);

            List<Long> ids = events.stream().map(Event::getId).collect(Collectors.toList());

            Map<Long, Long> confirmedRequests = requestRepository.findAllByEventIdInAndStatus(ids, CONFIRMED)
                    .stream()
                    .collect(Collectors.toMap(ConfirmedRequests::getEvent, ConfirmedRequests::getCount));

            for (Event event : events) {
                ObjectMapper mapper = new ObjectMapper();
                List<ViewStatsDto> statsDto = mapper.convertValue(response.getBody(), new TypeReference<>() {
                });

                if (!statsDto.isEmpty()) {
                    result.add(eventMapper.toEventShortDtoWithViews(event, statsDto.getFirst().getHits(),
                            confirmedRequests.getOrDefault(event.getId(), 0L)));
                } else {
                    result.add(eventMapper.toEventShortDtoWithViews(event, 0L,
                            confirmedRequests.getOrDefault(event.getId(), 0L)));
                }
            }

            saveEndpointHit(request);

        }
        return result;
    }

    private void saveEndpointHit(HttpServletRequest request) {

        EndpointHitDto hit = EndpointHitDto.builder()
                .app(app)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDtoWithViews getEventById(Long eventId, HttpServletRequest request) {

        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();

        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));

        ResponseEntity<Object> response = statsClient.getStats(event.getCreatedOn(), LocalDateTime.now(), List.of(uri), true);

        Long views = viewsParser(response);

        if (!event.getState().equals(State.PUBLISHED)) {
            throw new NotFoundException("Событие с id = " + eventId + " не найдено.");
        }

        EndpointHitDto hit = EndpointHitDto.builder()
                .app(app)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build();

        statsClient.saveHit(hit);

        return eventMapper.toDtoWithView(event, views);
    }

    private Long viewsParser(ResponseEntity<Object> response) {
        ObjectMapper mapper = new ObjectMapper();

        List<ViewStatsDto> viewStatsList = mapper.convertValue(response.getBody(), new TypeReference<>() {
        });

        long views = 0;

        if (viewStatsList != null) {
            for (ViewStatsDto stats : viewStatsList) {
                views = stats.getHits();
            }
        }

        return views;
    }

    @Override
    @Transactional
    public EventFullDto updateEventByOwner(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        Event event = eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));

        State stateEvent = event.getState();
        if (!stateEvent.equals(State.CANCELED) && !stateEvent.equals(PENDING)) {
            throw new ForbiddenException("Изменить можно только отмененные или в состоянии ожидания события.");
        }

        LocalDateTime eventDateTime = event.getEventDate();
        if (eventDateTime.isBefore(LocalDateTime.now().plusHours(2L))) {
            throw new ConflictException("Дата мероприятия должна быть как минимум на два часа позже.");
        }

        updateEvent(event, eventMapper.toAdminOrUser(updateEvent));

        Event saveEvent = eventRepository.save(event);


        return eventMapper.toDto(saveEvent);
    }

    @Transactional
    private void updateEvent(Event event, UpdateEventAdminRequest updateEvent) {
        if (updateEvent.getAnnotation() != null) {
            event.setAnnotation(updateEvent.getAnnotation());
        }

        if (updateEvent.getCategory() != null) {
            Long categoryId = updateEvent.getCategory();
            Category category = categoryRepository.findById(categoryId).orElseThrow(() ->
                    new NotFoundException("Категория с id = " + categoryId + " не найдена."));
            event.setCategory(category);

        }

        if (updateEvent.getDescription() != null) {
            event.setDescription(updateEvent.getDescription());
        }

        if (updateEvent.getEventDate() != null) {
            event.setEventDate(updateEvent.getEventDate());
        }

        if (updateEvent.getLocation() != null) {
            Location location = checkLocation(locationMapper.toLocation(updateEvent.getLocation()));
            event.setLocation(location);
        }

        if (updateEvent.getPaid() != null) {
            event.setPaid(updateEvent.getPaid());
        }

        if (updateEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(updateEvent.getParticipantLimit());
        }

        if (updateEvent.getRequestModeration() != null) {
            event.setRequestModeration(updateEvent.getRequestModeration());
        }

        if (updateEvent.getStateAction() != null) {
            StateAction stateAction = StateAction.valueOf(updateEvent.getStateAction());

            switch (stateAction) {
                case PUBLISH_EVENT:
                    publishEvent(event);
                    break;
                case REJECT_EVENT:
                    rejectEvent(event);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(PENDING);
                    break;
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;

            }
        }

        if (updateEvent.getTitle() != null) {
            event.setTitle(updateEvent.getTitle());
        }

    }

    private void publishEvent(Event event) {
        if (!event.getState().equals(PENDING)) {
            throw new ConflictException("Событие не может быть опубликовано, поскольку оно не находится на рассмотрении.");
        }
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        if (event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Событие не может быть отклонено, поскольку оно уже опубликовано.");
        }
        event.setState(State.CANCELED);
    }

}
