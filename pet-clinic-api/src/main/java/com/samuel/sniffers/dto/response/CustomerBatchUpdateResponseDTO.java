package com.samuel.sniffers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CustomerBatchUpdateResponseDTO {

    private Integer successfulUpdatesCount;

    private Integer failedUpdatesCount;

    private List<CustomerResponseDTO> successfulUpdates;

    private List<BatchUpdateFailure> failedUpdates;
}
