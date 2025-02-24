package com.samuel.sniffers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateItemDTO {

    @Size(min = 2, max = 100, message = "Description must be between 2 and 100 characters")
    private String description;

    @Min(value = 1, message = "Amount must be at least 1.")
    private Integer amount;
}
