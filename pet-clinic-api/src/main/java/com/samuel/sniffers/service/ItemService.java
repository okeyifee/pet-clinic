package com.samuel.sniffers.service;

import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;

import java.util.List;

public interface ItemService {

    ItemResponseDTO createItem(String customerId, String basketId, ItemDTO itemDTO);

    ItemResponseDTO getItem(String customerId, String basketId, String itemId);

    List<ItemResponseDTO> getAllItems(String customerId, String basketId);

    ItemResponseDTO updateItem(String customerId, String basketId, String itemId, ItemDTO dto);

    ItemResponseDTO updateItem(String customerId, String basketId, String itemId, UpdateItemDTO dto);

    ItemBatchUpdateResponseDTO batchUpdateItems(String customerId, String basketId, BatchItemUpdateDTO dto);

    void deleteItem(String customerId, String basketId, String itemId);
}
