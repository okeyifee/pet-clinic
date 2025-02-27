package com.samuel.sniffers.service.view;

import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;

import java.util.List;

public interface CustomerBasketItemViewService {

    List<CustomerBasketItemViewDTO> findAllWithAccess();

    List<CustomerBasketItemViewDTO> findByCustomerNameWithAccess(String customerName);
}
