package com.samuel.sniffers.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/*
  This class provides the request object to be used for Item entity.
  This allows specifying validations necessary for the API to handle.
*/
@Data
public class ItemDTO {

    @Size(min = 2, max = 100, message = "Description must be between 2 and 100 characters")
    private String description;

    @NotNull
    @Min(value = 1, message = "Amount must be at least 1.")
    private Integer amount;
}
