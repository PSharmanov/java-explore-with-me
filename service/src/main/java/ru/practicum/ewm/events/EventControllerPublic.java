package ru.practicum.ewm.events;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.events.dto.EventFullDtoWithViews;
import ru.practicum.ewm.events.dto.EventShortDtoWithViews;
import ru.practicum.ewm.events.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventControllerPublic {
    private final EventService eventService;

    @GetMapping
    public List<EventShortDtoWithViews> getEvents(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                  LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                  LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                                  @RequestParam(defaultValue = "EVENT_DATE") String sort,
                                                  @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero
                                                  Integer from,
                                                  @RequestParam(value = "size", defaultValue = "10") @Positive
                                                  Integer size,
                                                  HttpServletRequest request) {
        return eventService.getEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size, request);
    }

    @GetMapping("/{eventId}")
    public EventFullDtoWithViews getEventById(@PathVariable Long eventId, HttpServletRequest request) {
        return eventService.getEventById(eventId, request);
    }
}
