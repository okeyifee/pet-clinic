package com.samuel.sniffers.dto.response.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBasketItemViewDTO {

    private String customerId;

    private String customerName;

    private String customerTimezone;

    @JsonIgnore // Ignore returning ownerToken in response
    private String ownerToken;

    private LocalDateTime customerCreated;

    private String basketId;

    private String basketStatus;

    private LocalDateTime basketCreated;

    private LocalDateTime basketStatusDate;

    private String itemId;

    private String itemDescription;

    private Integer itemAmount;

    private LocalDateTime itemCreated;
}
