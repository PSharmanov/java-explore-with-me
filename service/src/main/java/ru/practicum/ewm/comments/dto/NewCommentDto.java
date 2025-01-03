package ru.practicum.ewm.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewCommentDto {
    @NotBlank(message = "Комментарий не может быть пустым или состоять из пробелов.")
    @Size(min = 1, max = 1000, message = "Размер комментариев должен быть от 1 до 1000 символов.")
    String text;
}
