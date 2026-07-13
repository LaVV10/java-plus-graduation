package ru.practicum.main.dto.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationDto {
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Float lat;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Float lon;
}