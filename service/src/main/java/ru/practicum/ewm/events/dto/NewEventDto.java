package ru.practicum.ewm.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.ewm.locations.LocationDto;

import java.time.LocalDateTime;

import static ru.practicum.ewm.util.DateConstant.DATE_TIME_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {

    @Size(min = 20, max = 2000, message = "Краткое описание события должно быть от 20 до 2000 символов.")
    @NotBlank(message = "Краткое описание события не должно быть пустым.")
    String annotation;

    @NotNull(message = "Категория события не может быть null.")
    Long category;

    @Size(min = 20, max = 7000, message = "Описание события должно быть от 20 до 2000 символов.")
    @NotBlank(message = "Описание события не должно быть пустым.")
    String description;

    @NotNull(message = "Дата события не может быть null.")
    @Future(message = "Дата события не может быть в прошлом.")
    @JsonFormat(pattern = DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    @NotNull(message = "Местоположение события не может быть null.")
    @Valid
    LocationDto location;

    @Getter
    boolean paid = false;

    @PositiveOrZero(message = "Ограничение на количество участников. Значение 0 - означает отсутствие ограничения.")
    int participantLimit = 0;

    @Getter
    boolean requestModeration = true;

    @Size(min = 3, max = 120, message = "Размер заголовка события должен быть от 3 до 120 символов.")
    @NotBlank(message = "Заголовок события не должен быть пустым.")
    String title;

}
