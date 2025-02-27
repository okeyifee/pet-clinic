package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.service.ShoppingBasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.net.URI;

@RestController
@RequestMapping(value = "/v1/customer/{customerId}/basket", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Basket controller", description = "APIs for managing customers basket")
public class ShoppingBasketController {

    private final ShoppingBasketService basketService;

    public ShoppingBasketController(ShoppingBasketService basketService) {
        this.basketService = basketService;
    }

    @Operation(summary = "Create shopping basket", description = "Creates a new shopping basket for the customer")
    @PostMapping
    public ResponseEntity<ApiResponse<BasketResponseDTO>> createBasket(@PathVariable String customerId) {
        BasketResponseDTO basket = basketService.createBasket(customerId);
        return ResponseEntity
                .created(URI.create(String.format("/api/v1/customer/%s/basket/%s", customerId, basket.getId())))
                .body(ApiResponse.created("Shopping basket created successfully.", basket));
    }

    @Operation(summary = "Retrieve a basket", description = "Fetches the details of a shopping basket by its ID")
    @GetMapping("/{basketId}")
    public ResponseEntity<ApiResponse<BasketResponseDTO>> getBasket(@PathVariable String customerId, @PathVariable String basketId) {
        return ResponseEntity.ok(
                ApiResponse.success("Basket retrieved successfully.",
                        basketService.getBasket(customerId, basketId)
                )
        );
    }

    @Operation(summary = "Get all baskets for a customer", description = "Retrieves a paginated list of all shopping baskets belonging to a customer")
    @GetMapping
    public ResponseEntity<PagedResponse<BasketResponseDTO>> getAllBaskets(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request) {

        PagedResponse<BasketResponseDTO> response = basketService.findAll(
                customerId, page, size, sortBy, direction, request.getRequestURL().toString());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Stream all shopping baskets", description = "Streams a list of shopping baskets as newline-delimited JSON")
    @GetMapping(
            value = "/stream",
            produces = {
                    "application/x-ndjson",
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.TEXT_PLAIN_VALUE
            })
    public ResponseEntity<StreamingResponseBody> streamAllCustomers(@PathVariable String customerId) {
        StreamingResponseBody responseBody = outputStream -> basketService.streamAllToResponse(outputStream, customerId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .body(responseBody);
    }

    @Operation(summary = "Update shopping basket", description = "Updates the shopping basket")
    @PatchMapping("/{basketId}")
    public ResponseEntity<ApiResponse<BasketResponseDTO>> updateBasket(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody UpdateBasketDTO dto) {
        BasketResponseDTO updatedBasket = basketService.updateBasket(customerId, basketId, dto);
        return ResponseEntity.ok(ApiResponse.success("Basket updated successfully.", updatedBasket));
    }

    @Operation(summary = "Update shopping baskets in batch", description = "Batch operation to update shopping baskets")
    @PatchMapping
    public ResponseEntity<ApiResponse<BasketBatchUpdateResponseDTO>> batchUpdateBasket(
            @PathVariable String customerId,
            @Valid @RequestBody BatchBasketUpdateDTO dto) {
        BasketBatchUpdateResponseDTO updatedBasket = basketService.batchUpdateBasket(customerId, dto);
        return ResponseEntity.ok(ApiResponse.success("Basket updated successfully.", updatedBasket));
    }

    @Operation(summary = "Check if a basket exists", description = "Checks if a shopping basket exists by its ID")
    @RequestMapping(value = "/{basketId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkBasketExists(@PathVariable String customerId, @PathVariable String basketId) {
        basketService.getBasket(customerId, basketId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a shopping basket", description = "Deletes a shopping basket by its ID")
    @DeleteMapping("/{basketId}")
    public ResponseEntity<ApiResponse<Void>> deleteBasket(@PathVariable String customerId, @PathVariable String basketId) {
        basketService.deleteBasket(customerId, basketId);
        return ResponseEntity.noContent().build();
    }
}
