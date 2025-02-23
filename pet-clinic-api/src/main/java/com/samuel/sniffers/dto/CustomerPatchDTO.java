package com.samuel.sniffers.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerPatchDTO {

    @Size(min = 2, max = 255, message = "Name must be between 2 and 255 characters")
    private String name;

    private String timezone;
}
