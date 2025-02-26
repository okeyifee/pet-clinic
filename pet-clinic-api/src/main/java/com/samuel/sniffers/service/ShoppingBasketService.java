package com.samuel.sniffers.service;

import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;

import java.io.OutputStream;
import java.util.List;

public interface ShoppingBasketService {

    BasketResponseDTO createBasket(String customerId);

    BasketResponseDTO getBasket(String customerId, String basketId);

    List<BasketResponseDTO> getAllBaskets(String customerId);

    void streamAllToResponse(OutputStream outputStream, String customerId);

    BasketResponseDTO updateBasket(String customerId, String basketId, UpdateBasketDTO dto);

    BasketBatchUpdateResponseDTO batchUpdateBasket(String customerId, BatchBasketUpdateDTO dto);

    void deleteBasket(String customerId, String basketId);
}
