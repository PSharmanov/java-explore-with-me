package ru.practicum.server.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.server.exception.RequestException;
import ru.practicum.server.mapper.EndpointHitMapper;
import ru.practicum.server.mapper.ViewStatsMapper;
import ru.practicum.server.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class StatServiceImpl implements StatService {
    private final StatsRepository statsRepository;

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {

        if (start == null || start.isEmpty()) {
            throw new RequestException("Дата начала выборки отсутствует!");
        }
        if (end == null || end.isEmpty()) {
            throw new RequestException("Дата окончания выборки отсутствует!");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDateTime = LocalDateTime.parse(start, formatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, formatter);

        if (startDateTime.isAfter(endDateTime)) {
            throw new RequestException("Дата начала позже даты окончания!");
        }

        PageRequest pageable = PageRequest.of(0, 10);

        if (unique) {
            log.info("Получаем статистику обращений с {} по {}", startDateTime, endDateTime);
            return ViewStatsMapper.mapToListDto(statsRepository.getUniqueHits(startDateTime, endDateTime, uris, pageable));
        } else {
            log.info("Получаем статистику уникальных обращений с {} по {}", startDateTime, endDateTime);
            return ViewStatsMapper.mapToListDto(statsRepository.getHits(startDateTime, endDateTime, uris, pageable));
        }
    }

    @Override
    public EndpointHitDto addHit(EndpointHitDto requestDto) {
        log.info("Сохраняем в статистику обращение из {} к {} с ip {}", requestDto.getApp(), requestDto.getUri(), requestDto.getId());
        return EndpointHitMapper.mapToDto(statsRepository.save(EndpointHitMapper.mapToEntity(requestDto)));
    }
}

