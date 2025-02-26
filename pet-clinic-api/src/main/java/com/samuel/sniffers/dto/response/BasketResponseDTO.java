package com.samuel.sniffers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.samuel.sniffers.enums.BasketStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

/*
  This class provides the response object to be used for Basket entity.
*/
@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class BasketResponseDTO {

    private String id;

    private BasketStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime statusDate;

    private Set<ItemResponseDTO> items;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;
}
