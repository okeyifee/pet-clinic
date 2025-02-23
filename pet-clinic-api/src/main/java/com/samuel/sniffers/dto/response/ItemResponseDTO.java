package com.samuel.sniffers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/*
  This class provides the response object to be used for Item entity.
*/
@Setter
@Getter
public class ItemResponseDTO {

    private String id;

    private String description;

    private Integer amount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}
