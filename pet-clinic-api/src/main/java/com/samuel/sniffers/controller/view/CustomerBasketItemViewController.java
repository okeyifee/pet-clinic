package com.samuel.sniffers.controller.view;

import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;
import com.samuel.sniffers.service.view.CustomerBasketItemViewService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/view/customer-basket-items")
public class CustomerBasketItemViewController {

    private final CustomerBasketItemViewService service;

    public CustomerBasketItemViewController(CustomerBasketItemViewService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerBasketItemViewDTO>>> getAllViewEntries() {
        return ResponseEntity.ok(ApiResponse.success(service.findAllWithAccess()));
    }

    @Operation(summary = "Get all customer->basket->item views paginated", description = "Retrieves a paginated list of customer->basket->item views")
    @GetMapping("/paginated")
    public ResponseEntity<PagedResponse<CustomerBasketItemViewDTO>> getAllCustomerViewsPaginated(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request) {

        PagedResponse<CustomerBasketItemViewDTO> paginatedViews = service.getPaginatedView(page, size, sortBy, direction, request.getRequestURL().toString());

        return ResponseEntity.ok(paginatedViews);
    }

    @GetMapping("/by-customer-name/{customerName}")
    public ResponseEntity<ApiResponse<List<CustomerBasketItemViewDTO>>> getByCustomerName(@PathVariable String customerName) {

        // Admin can return customers from different tenant since name is unique per tenant (tenant is based on token)
        List<CustomerBasketItemViewDTO> views = service.findByCustomerNameWithAccess(customerName);

        return ResponseEntity.ok(ApiResponse.success(views));
    }
}