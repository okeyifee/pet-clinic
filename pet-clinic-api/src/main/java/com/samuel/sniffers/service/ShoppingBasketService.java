package com.samuel.sniffers.service;

import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.entity.ShoppingBasket;

import java.io.OutputStream;

public interface ShoppingBasketService {

    BasketResponseDTO createBasket(String customerId);

    BasketResponseDTO getBasket(String customerId, String basketId);

    PagedResponse<BasketResponseDTO> findAll(String customerId, int page, int size, String sortBy, String direction, String baseUrl);

    void streamAllToResponse(OutputStream outputStream, String customerId);

    BasketResponseDTO updateBasket(String customerId, String basketId, UpdateBasketDTO dto);

    BasketBatchUpdateResponseDTO batchUpdateBasket(String customerId, BatchBasketUpdateDTO dto);

    void deleteBasket(String customerId, String basketId);

    ShoppingBasket getDbBasket(String customerId, String basketId);

    boolean basketExist(String customerId, String basketId);
}
