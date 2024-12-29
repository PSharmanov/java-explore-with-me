package ru.practicum.ewm.locations;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    @NotNull(message = "Не задана широта места проведения события.")
    private Float lat;

    @NotNull(message = "Не задана долгота места проведения события.")
    private Float lon;
}
