package com.samuel.sniffers.controller.view;

import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.dto.response.view.CustomerBasketItemViewDTO;
import com.samuel.sniffers.service.view.CustomerBasketItemViewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/by-customer-name/{customerName}")
    public ResponseEntity<ApiResponse<List<CustomerBasketItemViewDTO>>> getByCustomerName(@PathVariable String customerName) {

        // Admin can return customers from different tenant since name is unique per tenant (tenant is based on token)
        List<CustomerBasketItemViewDTO> views = service.findByCustomerNameWithAccess(customerName);

        return ResponseEntity.ok(ApiResponse.success(views));
    }
}