package ru.practicum.ewm.comments.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewm.events.dto.EventShortDto;
import ru.practicum.ewm.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateConstant.DATE_TIME_PATTERN;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private UserShortDto author;
    private EventShortDto event;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime created;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime updated;
}