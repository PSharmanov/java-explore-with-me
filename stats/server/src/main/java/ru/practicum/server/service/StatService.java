package ru.practicum.server.service;

import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;

import java.util.List;

public interface StatService {
    List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique);

    EndpointHitDto addHit(EndpointHitDto requestDto);
}
