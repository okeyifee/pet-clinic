package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.CustomerBatchUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.CustomerPatchDTO;
import com.samuel.sniffers.dto.response.CustomerBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.service.CustomerService;
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
@RequestMapping(value = "/v1/customer", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customer controller", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create customer", description = "Creates a customer")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        CustomerResponseDTO created = customerService.create(customerDTO);
        return ResponseEntity
                .created(URI.create("/api/v1/customers/" + created.getId()))
                .body(ApiResponse.created("Customer created successfully", created));
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves customer details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getCustomer(@PathVariable String id) {
        CustomerResponseDTO customer = customerService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @Operation(summary = "Get all customers", description = "Retrieves a paginated list of customers")
    @GetMapping
    public ResponseEntity<PagedResponse<CustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            HttpServletRequest request) {

        PagedResponse<CustomerResponseDTO> response = customerService.findAll(
                page, size, sortBy, direction, request.getRequestURL().toString());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Stream all customers", description = "Streams a list of customers as newline-delimited JSON")
    @GetMapping(
            value = "/stream",
            produces = {
            "application/x-ndjson",
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.TEXT_PLAIN_VALUE
    })
    public ResponseEntity<StreamingResponseBody> streamAllCustomers() {
        StreamingResponseBody responseBody = customerService::streamAllToResponse;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/x-ndjson"))
                .body(responseBody);
    }

    @Operation(summary = "Update customer", description = "Updates customer information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        CustomerResponseDTO updated = customerService.update(id, customerDTO);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @Operation(summary = "Partially update customer", description = "Allows partial updates to customer data")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomerPartially(
            @PathVariable String id,
            @Valid @RequestBody CustomerPatchDTO patchDTO) {
        CustomerResponseDTO updated = customerService.update(id, patchDTO);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @Operation(summary = "Batch update customers", description = "Allows batch updates of customer data")
    @PatchMapping("/batch")
    public ResponseEntity<ApiResponse<CustomerBatchUpdateResponseDTO>> batchUpdateCustomers(
            @Valid @RequestBody CustomerBatchUpdateDTO batchUpdateDTO) {
        CustomerBatchUpdateResponseDTO updatedCustomers = customerService.batchUpdate(batchUpdateDTO);
        return ResponseEntity.ok(ApiResponse.success("Batch operation executed successfully", updatedCustomers));
    }

    @Operation(summary = "Delete customer", description = "Deletes customer record")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable String id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkCustomerExists(@PathVariable String id) {
        customerService.findById(id);
        return ResponseEntity.noContent().build();
    }
}
