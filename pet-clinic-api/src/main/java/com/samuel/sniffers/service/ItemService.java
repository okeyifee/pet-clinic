package com.samuel.sniffers.service;

import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;

import java.io.OutputStream;

public interface ItemService {

    ItemResponseDTO createItem(String customerId, String basketId, ItemDTO itemDTO);

    ItemResponseDTO getItem(String customerId, String basketId, String itemId);

    PagedResponse<ItemResponseDTO> findAll(String customerId, String basketId, int page, int size, String sortBy, String direction, String baseUrl);

    void streamAllToResponse(OutputStream outputStream, String customerId, String basketId);

    ItemResponseDTO updateItem(String customerId, String basketId, String itemId, ItemDTO dto);

    ItemResponseDTO updateItem(String customerId, String basketId, String itemId, UpdateItemDTO dto);

    ItemBatchUpdateResponseDTO batchUpdateItems(String customerId, String basketId, BatchItemUpdateDTO dto);

    void deleteItem(String customerId, String basketId, String itemId);
}
