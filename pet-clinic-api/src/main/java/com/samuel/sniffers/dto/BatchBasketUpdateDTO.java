package com.samuel.sniffers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchBasketUpdateDTO {

    @Valid
    private List<BasketPatchDTO> updates;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class BasketPatchDTO extends UpdateBasketDTO {

        @NotEmpty(message = "Basket ID is required")
        @NotNull
        private String basketId;
    }
}
