package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.service.ShoppingBasketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/customer/{customerId}/basket", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Basket controller", description = "APIs for managing customers basket")
public class ShoppingBasketController {

    private final ShoppingBasketService basketService;
    private final Logger logger;

    public ShoppingBasketController(ShoppingBasketService basketService) {
        this.basketService = basketService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Operation(summary = "Create shopping basket", description = "Creates a new shopping basket for the customer")
    @PostMapping
    public ResponseEntity<ApiResponse<BasketResponseDTO>> createBasket(@PathVariable String customerId) {
        logger.debug("Processing request to create basket...");
        BasketResponseDTO basket = basketService.createBasket(customerId);
        logger.debug("Completed request to create basket. Id {}.", basket.getId());
        return ResponseEntity
                .created(URI.create(String.format("/api/v1/customer/%s/basket/%s", customerId, basket.getId())))
                .body(ApiResponse.created("Shopping basket created successfully.", basket));
    }

    @Operation(summary = "Retrieve a basket", description = "Fetches the details of a shopping basket by its ID")
    @GetMapping("/{basketId}")
    public ResponseEntity<ApiResponse<BasketResponseDTO>> getBasket(@PathVariable String customerId, @PathVariable String basketId) {
        logger.debug("Processing request to get basket with id {} for customer {}.", basketId, customerId);
        BasketResponseDTO basket = basketService.getBasket(customerId, basketId);
        logger.debug("Completed request to get basket with id {} for customer {}.", basketId, customerId);

        return ResponseEntity.ok(ApiResponse.success("Basket retrieved successfully.", basket));
    }

    @Operation(summary = "Retrieve all baskets for a customer", description = "Fetches all shopping baskets belonging to a customer")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BasketResponseDTO>>> getCustomerBaskets(@PathVariable String customerId) {
        logger.debug("Processing request to get all baskets for customer with id {}", customerId);
        List<BasketResponseDTO> baskets = basketService.getAllBaskets(customerId);
        logger.debug("Completed request to get all baskets for customer with id {}", customerId);

        return ResponseEntity.ok(ApiResponse.success("Baskets retrieved successfully.", baskets));
    }

    @Operation(summary = "Update shopping basket", description = "Updates the shopping basket")
    @PatchMapping("/{basketId}")
    public ResponseEntity<ApiResponse<BasketResponseDTO>> updateBasket(
            @PathVariable String customerId,
            @PathVariable String basketId,
            @Valid @RequestBody UpdateBasketDTO dto) {
        logger.debug("Processing request to update basket with id {} for customer {}.", basketId, customerId);
        BasketResponseDTO updatedBasket = basketService.updateBasket(customerId, basketId, dto);
        logger.debug("Completed request to update basket with id {} for customer {}.", basketId, customerId);

        return ResponseEntity.ok(ApiResponse.success("Basket updated successfully.", updatedBasket));
    }

    @Operation(summary = "Update shopping baskets in batch", description = "Batch operation to update shopping baskets")
    @PatchMapping
    public ResponseEntity<ApiResponse<BasketBatchUpdateResponseDTO>> batchUpdateBasket(
            @PathVariable String customerId,
            @Valid @RequestBody BatchBasketUpdateDTO dto) {
        logger.debug("Processing batch request to update baskets for customer with id {}", customerId);
        BasketBatchUpdateResponseDTO updatedBasket = basketService.batchUpdateBasket(customerId, dto);
        logger.debug("Completed batch request to update baskets for customer with id {}", customerId);

        return ResponseEntity.ok(ApiResponse.success("Basket updated successfully.", updatedBasket));
    }

    @Operation(summary = "Check if a basket exists", description = "Checks if a shopping basket exists by its ID")
    @RequestMapping(value = "/{basketId}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkBasketExists(@PathVariable String customerId, @PathVariable String basketId) {
        logger.debug("Processing request to check if basket with id {} exist for customer {}.", basketId, customerId);
        basketService.getBasket(customerId, basketId);
        logger.debug("Completed request to check if basket with id {} exist for customer {}.", basketId, customerId);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete a shopping basket", description = "Deletes a shopping basket by its ID")
    @DeleteMapping("/{basketId}")
    public ResponseEntity<ApiResponse<Void>> deleteBasket(@PathVariable String customerId, @PathVariable String basketId) {
        logger.debug("Processing request to delete basket {} for customer {}.", basketId, customerId);
        basketService.deleteBasket(customerId, basketId);
        logger.debug("Completed request to delete basket {} for customer {}.", basketId, customerId);

        return ResponseEntity.noContent().build();
    }
}
