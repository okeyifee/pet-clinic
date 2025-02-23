package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.InvalidRequestException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.BatchUpdateFailure;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.Item;
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.repository.ItemRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String ITEM_NOT_FOUND = "Item not found or access denied";

    private final ItemRepository itemRepository;
    private final CustomerRepository customerRepository;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    @Override
    public ItemResponseDTO createItem(String customerId, String basketId, ItemDTO dto) {
        Customer customer = getCustomer(customerId);

        customer.getBaskets()
                .stream()
                .filter(basket -> basket.getId().equalsIgnoreCase(basketId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        final Item item = entityFactory.convertToEntity(dto, Item.class);
        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    public ItemResponseDTO getItem(String customerId, String basketId, String itemId) {
        getCustomer(customerId);

        return itemRepository.findByIdWithAccess(
                itemId,
                basketId,
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .map(item -> entityFactory.convertToDTO(item, ItemResponseDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
    }

    @Override
    public List<ItemResponseDTO> getAllItems(String customerId, String basketId) {
        getCustomer(customerId);
        return itemRepository.findByCustomerWithAccess(
                basketId,
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .stream()
                .map(item -> entityFactory.convertToDTO(item, ItemResponseDTO.class))
                .toList();
    }

    @Override
    public ItemResponseDTO updateItem(String customerId, String basketId, String itemId, ItemDTO dto) {
        getCustomer(customerId);
        Item item = itemRepository.findByIdWithAccess(
                        itemId,
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        item.setAmount(dto.getAmount());
        item.setDescription(dto.getDescription());

        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    public ItemResponseDTO updateItem(String customerId, String basketId, String itemId, UpdateItemDTO dto) {

        if (dto.getDescription() == null && dto.getAmount() == null) {
            throw new InvalidRequestException("You must provide either 'description' or 'amount' in the PATCH request. Both fields cannot be empty.");
        }

        getCustomer(customerId);
        Item item = itemRepository.findByIdWithAccess(
                        itemId,
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        item = entityFactory.patchEntity(dto, item);
        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    public ItemBatchUpdateResponseDTO batchUpdateItems(String customerId, String basketId, BatchItemUpdateDTO dto) {
        getCustomer(customerId);

        final List<String> itemIds = dto.getUpdates().stream()
                .map(BatchItemUpdateDTO.ItemPatchDTO::getItemId)
                .toList();

        List<Item> items = itemRepository.findByCustomerIdAndBasketIds(
                customerId,
                basketId,
                itemIds,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        );

        Map<String, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getId, b -> b));

        List<Item> updatedItems = new ArrayList<>();
        List<BatchUpdateFailure> failedUpdates = new ArrayList<>();

        for (BatchItemUpdateDTO.ItemPatchDTO updateRequest : dto.getUpdates()) {
            try {
                Item item = itemMap.get(updateRequest.getItemId());

                if (item == null) {
                    failedUpdates.add(new BatchUpdateFailure(updateRequest.getItemId(), ITEM_NOT_FOUND));
                    continue;
                }

                entityFactory.patchEntity(updateRequest, item);
                updatedItems.add(item);
            } catch (Exception ex) {
                failedUpdates.add(new BatchUpdateFailure(updateRequest.getItemId(), "Failed to update item: " + ex.getMessage()));
            }
        }

        itemRepository.saveAll(updatedItems);
        return new ItemBatchUpdateResponseDTO(
                !updatedItems.isEmpty() ? updatedItems.size() : null,
                !failedUpdates.isEmpty() ? failedUpdates.size() : null,
                entityFactory.convertToEntityList(updatedItems, ItemResponseDTO.class),
                failedUpdates
        );
    }

    @Override
    public void deleteItem(String customerId, String basketId, String itemId) {
        getCustomer(customerId);
        Item item = itemRepository.findByIdWithAccess(
                        itemId,
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));

        logger.info("deleting item {}", item);
        itemRepository.delete(item);
        logger.info("deleted item {}", item);
    }

    private Customer getCustomer(String customerId) {
        return customerRepository.findByIdWithAccess(
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        ).orElseThrow(
                () -> new ResourceNotFoundException("Customer not found")
        );
    }
}
