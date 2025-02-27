package com.samuel.sniffers.service.view;

import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;

import java.util.List;

public interface CustomerBasketItemViewService {

    List<CustomerBasketItemViewDTO> findAllWithAccess();

    PagedResponse<CustomerBasketItemViewDTO> getPaginatedView(int page, int size, String sortBy, String direction, String baseUrl);

    List<CustomerBasketItemViewDTO> findByCustomerNameWithAccess(String customerName);
}
