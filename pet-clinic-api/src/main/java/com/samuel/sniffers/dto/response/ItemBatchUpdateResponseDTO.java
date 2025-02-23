package com.samuel.sniffers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemBatchUpdateResponseDTO {

    private Integer successfulUpdatesCount;

    private Integer failedUpdatesCount;

    private List<ItemResponseDTO> successfulUpdates;

    private List<BatchUpdateFailure> failedUpdates;
}
