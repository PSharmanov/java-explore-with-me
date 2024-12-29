package ru.practicum.ewm.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.ewm.util.DateConstant.DATE_TIME_PATTERN;

@Getter
@Builder
public class ApiError {
    private String status;
    private String reason;
    private String message;
    private List<String> errors;
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    private LocalDateTime timestamp;

}
