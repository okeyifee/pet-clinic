package com.samuel.sniffers.dto;

import com.samuel.sniffers.enums.BasketStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateBasketDTO {

    private BasketStatus status;
}
