package ru.practicum.server.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import ru.practicum.server.mapper.DateTimeMapper;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private String status;
    private String reason;
    private String message;
    private String timestamp;

    public ErrorResponse(String status, String reason, String message) {
        this(status, reason, message, DateTimeMapper.toStringDate(LocalDateTime.now()));
    }
}