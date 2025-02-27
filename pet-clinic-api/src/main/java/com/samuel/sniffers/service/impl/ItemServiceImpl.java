package com.samuel.sniffers.service.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.samuel.sniffers.api.exception.InvalidRequestException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.exception.StreamingException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.BatchUpdateFailure;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.Item;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.repository.ItemRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
import com.samuel.sniffers.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ItemServiceImpl extends AbstractPaginationService implements ItemService {

    private static final String ITEM_NOT_FOUND = "Item not found or access denied";

    private final Logger logger;
    private final ItemRepository itemRepository;
    private final CustomerService customerService;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    public ItemServiceImpl(ItemRepository itemRepository, CustomerService customerService, SecurityService securityService, EntityFactory entityFactory) {
        this.itemRepository = itemRepository;
        this.customerService = customerService;
        this.securityService = securityService;
        this.entityFactory = entityFactory;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemResponseDTO createItem(String customerId, String basketId, ItemDTO dto) {
        Customer customer = customerService.getCustomer(customerId);

        ShoppingBasket userBasket = customer.getBaskets()
                .stream()
                .filter(basket -> basket.getId().equalsIgnoreCase(basketId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Basket not found"));

        Item item = entityFactory.convertToEntity(dto, Item.class);
        item.setBasket(userBasket);
        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemResponseDTO getItem(String customerId, String basketId, String itemId) {
        validateCustomerExists(customerId);

        return entityFactory.convertToDTO(getDatabaseItem( customerId, basketId, itemId), ItemResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PagedResponse<ItemResponseDTO> findAll(String customerId, String basketId,
            int page, int size, String sortBy, String direction, String baseUrl) {

        validateCustomerExists(customerId);

        // Sanitize and Create page request -> Avoid attacks on db via query params
        PageRequest pageRequest = PageRequest.of(
                Math.max(0, page - 1), // Spring Data's pagination follows a zero-based indexing approach
                size,
                getSanitizedSortDirection(direction),
                getSanitizedItemSortBy(sortBy)
        );

        String token = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(token);

        Page<Item> itemsPage = itemRepository.findByCustomerWithAccess(basketId, customerId, token, isAdmin, pageRequest);

        // Map entities to DTOs
        List<ItemResponseDTO> itemDTOs = itemsPage.getContent().stream()
                .map(item -> entityFactory.convertToDTO(item, ItemResponseDTO.class))
                .toList();

        // Create paged response
        PagedResponse<ItemResponseDTO> response = new PagedResponse<>(
                itemDTOs,
                itemsPage.getNumber() + 1,
                itemsPage.getSize(),
                itemsPage.getTotalElements(),
                itemsPage.getTotalPages(),
                itemsPage.isLast()
        );

        // Add pagination links
        response.setLinks(buildPaginationLinks(baseUrl, size, sortBy, direction, page, itemsPage));

        return response;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void streamAllToResponse(OutputStream outputStream, String customerId, String basketId) {
        String token = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(token);

        try {
            ObjectMapper objectMapper = entityFactory.getObjectMapperForStreaming();
            JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream);

            try (Stream<Item> customerStream = itemRepository.streamAllWithAccess(basketId, customerId, token, isAdmin)) {
                customerStream.forEach(item -> {
                    try {
                        ItemResponseDTO dto = entityFactory.convertToDTO(item, ItemResponseDTO.class);
                        objectMapper.writeValue(jsonGenerator, dto);
                        jsonGenerator.writeRaw('\n');
                        jsonGenerator.flush();
                    } catch (IOException e) {
                        throw new StreamingException("Error streaming item data", e);
                    }
                });
            }
        } catch (IOException e) {
            throw new StreamingException("Error initializing JSON generator", e);
        }
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemResponseDTO updateItem(String customerId, String basketId, String itemId, ItemDTO dto) {
        validateCustomerExists(customerId);
        Item item = getDatabaseItem(customerId, basketId, itemId);

        item.setAmount(dto.getAmount());
        item.setDescription(dto.getDescription());

        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemResponseDTO updateItem(String customerId, String basketId, String itemId, UpdateItemDTO dto) {

        if (dto.getDescription() == null && dto.getAmount() == null) {
            throw new InvalidRequestException("You must provide either 'description' or 'amount' in the PATCH request. Both fields cannot be empty.");
        }

        validateCustomerExists(customerId);
        Item item = getDatabaseItem(customerId, basketId, itemId);
        item = entityFactory.patchEntity(dto, item);
        return entityFactory.convertToDTO(itemRepository.save(item), ItemResponseDTO.class);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ItemBatchUpdateResponseDTO batchUpdateItems(String customerId, String basketId, BatchItemUpdateDTO dto) {
        validateCustomerExists(customerId);

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
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void deleteItem(String customerId, String basketId, String itemId) {
        validateCustomerExists(customerId);
        Item item = getDatabaseItem(customerId, basketId, itemId);

        itemRepository.delete(item);
        logger.info("deleted item with id {}", itemId);
    }

    private void validateCustomerExists(String customerId) {
        if (!customerService.customerExist(customerId)) {
            logger.error("Customer with id: {} not found", customerId);
            throw new ResourceNotFoundException("Customer not found");
        }
    }

    private Item getDatabaseItem(String customerId, String basketId, String itemId) {
        return itemRepository.findByIdWithAccess(
                        itemId,
                        basketId,
                        customerId,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(ITEM_NOT_FOUND));
    }
}
