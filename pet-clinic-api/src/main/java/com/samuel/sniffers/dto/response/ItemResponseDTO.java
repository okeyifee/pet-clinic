package com.samuel.sniffers.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/*
  This class provides the response object to be used for Item entity.
*/
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemResponseDTO {

    private String id;

    private String description;

    private Integer amount;
}
