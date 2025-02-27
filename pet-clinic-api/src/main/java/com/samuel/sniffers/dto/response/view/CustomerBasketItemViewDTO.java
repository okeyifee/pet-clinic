package com.samuel.sniffers.dto.response.view;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime customerCreated;

    private String basketId;

    private String basketStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime basketCreated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime basketStatusDate;

    private String itemId;

    private String itemDescription;

    private Integer itemAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime itemCreated;
}
