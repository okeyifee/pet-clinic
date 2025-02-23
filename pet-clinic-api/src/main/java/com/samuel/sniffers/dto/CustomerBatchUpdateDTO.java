package com.samuel.sniffers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerBatchUpdateDTO {

    @NotEmpty(message = "Customer updates list cannot be empty")
    @Valid
    private List<CustomerBatchPatchDTO> updates;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class CustomerBatchPatchDTO extends CustomerPatchDTO {

        @NotEmpty(message = "Customer ID is required")
        private String id;
    }
}

