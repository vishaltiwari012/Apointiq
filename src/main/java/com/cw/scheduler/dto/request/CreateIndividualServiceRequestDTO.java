package com.cw.scheduler.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateIndividualServiceRequestDTO {
    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @Min(1)
    private int durationMinutes;

    @DecimalMin("0.0")
    private double price;
}
