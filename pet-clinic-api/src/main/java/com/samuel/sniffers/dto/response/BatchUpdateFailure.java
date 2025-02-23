package com.samuel.sniffers.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchUpdateFailure {

    private String id;

    private String error;
}
