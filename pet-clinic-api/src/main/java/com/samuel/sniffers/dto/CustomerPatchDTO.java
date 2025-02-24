package com.samuel.sniffers.dto;

import com.samuel.sniffers.validator.ValidZoneId;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerPatchDTO {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @ValidZoneId
    private String timezone;
}
