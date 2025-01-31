package ru.practicum.ewm.compilations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewCompilationDto {

    private List<Long> events;

    private boolean pinned = false;

    @NotBlank
    @Size(min = 1, max = 50, message = "Размер заголовка должен быть от 1 до 50 символов.")
    private String title;

}
