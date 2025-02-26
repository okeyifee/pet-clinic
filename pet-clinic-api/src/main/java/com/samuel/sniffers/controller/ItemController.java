package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;
import com.samuel.sniffers.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/customer/{customerId}/basket/{basketId}/item", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Item controller", description = "APIs for managing basket items")
public class ItemController {

    private final ItemService itemService;
    private final Logger logger;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Operation(summary = "Create basket item", description = "Creates an item in a basket")
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponseDTO>> createItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody ItemDTO dto) {
        logger.debug("Processing request to create basket item...");
        ItemResponseDTO createdItem = itemService.createItem(customerId, basketId, dto);
        logger.debug("Completed request to create basket item. Id {}.", createdItem.getId());
        return ResponseEntity
                .created(URI.create(String.format("/api/v1/customer/%s/basket/%s/item/%s", customerId, basketId, createdItem.getId())))
                .body(ApiResponse.created("Item created successfully.", createdItem));
    }

    @Operation(summary = "Retrieve an item", description = "Fetches the details of an item using id.")
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponseDTO>> getItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId) {

        logger.debug("Processing request to get basket item with id {} for customer {} with basket {}", itemId, customerId, basketId);
        ItemResponseDTO item = itemService.getItem(customerId, basketId, itemId);
        logger.debug("Processing request to create basket item...");
        return ResponseEntity.ok(ApiResponse.success("Item retrieved successfully.", item)
        );
    }

    @Operation(summary = "Retrieve all items", description = "Retrieves all items in a given basket for a given customer")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponseDTO>>> getItems(
            @PathVariable String customerId,
            @PathVariable String basketId
    ) {
        logger.debug("Processing request to get all items for customer {} with basket id {}", customerId, basketId);
        List<ItemResponseDTO> items = itemService.getAllItems(customerId, basketId);
        logger.debug("Completed request to get all items for customer {} with basket id {}", customerId, basketId);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved successfully.", items));
    }

    @Operation(summary = "Update an item", description = "Updates the given item")
    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponseDTO>> updateItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateItemDTO dto) {
        logger.debug("Processing request to PATCH item {} for customer {} with basket id {}", itemId, customerId, basketId);
        ItemResponseDTO updatedItem = itemService.updateItem(customerId, basketId, itemId, dto);
        logger.debug("Completed request to PATCH item {} for customer {} with basket id {}", itemId, customerId, basketId);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully.", updatedItem));
    }

    @Operation(summary = "Update item", description = "Update item in a basket")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponseDTO>> updateItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId,
            @Valid @RequestBody ItemDTO dto) {
        logger.debug("Processing request to update item {} for customer {} with basket id {}", itemId, customerId, basketId);
        ItemResponseDTO updatedItem = itemService.updateItem(customerId, basketId, itemId, dto);
        logger.debug("Completed request to update item {} for customer {} with basket id {}", itemId, customerId, basketId);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully.", updatedItem));
    }

    @Operation(summary = "Update items in batch", description = "Batch operation to update items in a basket")
    @PatchMapping
    public ResponseEntity<ApiResponse<ItemBatchUpdateResponseDTO>> batchUpdateItems(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody BatchItemUpdateDTO dto) {
        logger.debug("Processing batch request to update items for customer {} with basket id {}", customerId, basketId);
        ItemBatchUpdateResponseDTO updatedItems = itemService.batchUpdateItems(customerId, basketId, dto);
        logger.debug("Completed batch request to update items for customer {} with basket id {}", customerId, basketId);
        return ResponseEntity.ok(ApiResponse.success("Items updated successfully.", updatedItems));
    }

    @Operation(summary = "Check if an item exists", description = "Checks if an item exists by Id")
    @RequestMapping(value = "/{itemId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkItemExists(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId) {
        logger.debug("Processing request to check if item with id {} exist for customer {} in basket with id {}", itemId, customerId, basketId);
        itemService.getItem(customerId, basketId, itemId);
        logger.debug("Completed request to check if item with id {} exist for customer {} in basket with id {}", itemId, customerId, basketId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete an item", description = "Deletes an item from a basket")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId
    ) {
        logger.debug("Processing request to delete item {} for customer {} from basket with id {}", itemId, customerId, basketId);
        itemService.deleteItem(customerId, basketId, itemId);
        logger.debug("Completed request to delete item {} for customer {} from basket with id {}", itemId, customerId, basketId);
        return ResponseEntity.noContent().build();
    }
}
