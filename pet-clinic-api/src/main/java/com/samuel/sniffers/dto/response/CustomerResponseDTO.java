package com.samuel.sniffers.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/*
  This class provides the response object to be used for Customer entity.
*/
@Setter
@Getter
public class CustomerResponseDTO {

    private String id;

    private String name;

    private String timezone;

    private List<BasketResponseDTO> baskets;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonIgnore // Ignore returning ownerToken in response
    private String ownerToken;
}
