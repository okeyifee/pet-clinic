package com.samuel.sniffers.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Valid
public class BatchItemUpdateDTO {

    @Valid
    private List<BatchItemUpdateDTO.ItemPatchDTO> updates;

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ItemPatchDTO extends UpdateItemDTO {

        @NotEmpty(message = "Item Id is required")
        @NotNull
        private String itemId;
    }
}
