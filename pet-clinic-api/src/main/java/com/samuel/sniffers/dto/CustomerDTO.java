package com.samuel.sniffers.dto;

import com.samuel.sniffers.validator.ValidZoneId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
  This class provides the request object to be used for Customer entity.
  This allows specifying validations necessary for the API to handle.
*/
@Data
public class CustomerDTO {

    @NotBlank(message = "Name is required.")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @ValidZoneId
    private String timezone;
}
