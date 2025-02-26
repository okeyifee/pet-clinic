package com.samuel.sniffers.controller;

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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;
import java.util.List;


@RestController
@RequestMapping(value = "/v1/customer/{customerId}/basket/{basketId}/item", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Item controller", description = "APIs for managing basket items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "Create basket item", description = "Creates an item in a basket")
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponseDTO>> createItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody ItemDTO dto) {
        ItemResponseDTO createdItem = itemService.createItem(customerId, basketId, dto);
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
        return ResponseEntity.ok(
                ApiResponse.success("Item retrieved successfully.",
                        itemService.getItem(customerId, basketId, itemId)
                )
        );
    }

    @Operation(summary = "Retrieve all items", description = "Retrieves all items in a given basket for a given customer")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponseDTO>>> getItems(
            @PathVariable String customerId,
            @PathVariable String basketId
    ) {
        List<ItemResponseDTO> items = itemService.getAllItems(customerId, basketId);
        return ResponseEntity.ok(ApiResponse.success("Items retrieved successfully.", items));
    }

    @Operation(summary = "Stream all Items", description = "Streams a list of items as newline-delimited JSON")
    @GetMapping(
            value = "/stream",
            produces = {
                    "application/x-ndjson",
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.TEXT_PLAIN_VALUE
            })
    public ResponseEntity<StreamingResponseBody> streamAllCustomers(@PathVariable String customerId,
                                                                    @PathVariable String basketId) {

        StreamingResponseBody responseBody = outputStream -> itemService.streamAllToResponse(outputStream, customerId, basketId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .body(responseBody);
    }

    @Operation(summary = "Update an item", description = "Updates the given item")
    @PatchMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponseDTO>> updateItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateItemDTO dto) {
        ItemResponseDTO updatedItem = itemService.updateItem(customerId, basketId, itemId, dto);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully.", updatedItem));
    }

    @Operation(summary = "Update item", description = "Update item in a basket")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<ItemResponseDTO>> updateItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId,
            @Valid @RequestBody ItemDTO dto) {
        ItemResponseDTO updatedItem = itemService.updateItem(customerId, basketId, itemId, dto);
        return ResponseEntity.ok(ApiResponse.success("Item updated successfully.", updatedItem));
    }

    @Operation(summary = "Update items in batch", description = "Batch operation to update items in a basket")
    @PatchMapping
    public ResponseEntity<ApiResponse<ItemBatchUpdateResponseDTO>> batchUpdateItems(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody BatchItemUpdateDTO dto) {
        ItemBatchUpdateResponseDTO updatedItems = itemService.batchUpdateItems(customerId, basketId, dto);
        return ResponseEntity.ok(ApiResponse.success("Items updated successfully.", updatedItems));
    }

    @Operation(summary = "Check if an item exists", description = "Checks if an item exists by Id")
    @RequestMapping(value = "/{itemId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkItemExists(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId) {
        itemService.getItem(customerId, basketId, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete an item", description = "Deletes an item from a basket")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @PathVariable String itemId
    ) {
        itemService.deleteItem(customerId, basketId, itemId);
        return ResponseEntity.noContent().build();
    }
}
