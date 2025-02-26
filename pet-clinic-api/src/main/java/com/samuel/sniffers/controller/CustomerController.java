package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.api.response.ApiResponse;
import com.samuel.sniffers.dto.CustomerBatchUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.CustomerPatchDTO;
import com.samuel.sniffers.dto.response.CustomerBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(value = "/v1/customer", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Customer controller", description = "APIs for managing customers")
public class CustomerController {

    private final CustomerService customerService;
    private final Logger logger;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Operation(summary = "Create customer", description = "Creates a customer")
    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> createCustomer(@Valid @RequestBody CustomerDTO customerDTO) {
        logger.debug("Processing request to create customer...");
        CustomerResponseDTO created = customerService.create(customerDTO);
        logger.debug("Successfully created a new customer with id {}", created.getId());
        return ResponseEntity
                .created(URI.create("/api/v1/customers/" + created.getId()))
                .body(ApiResponse.created("Customer created successfully", created));
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves customer details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> getCustomer(@PathVariable String id) {
        logger.debug("Processing request to retrieve customer by id...");
        CustomerResponseDTO customer = customerService.findById(id);
        logger.debug("Completed request to get customer with id {}", id);
        return ResponseEntity.ok(ApiResponse.success(customer));
    }

    @Operation(summary = "Get all customers", description = "Retrieves a list of customers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerResponseDTO>>> getAllCustomers() {
        logger.debug("Processing request to get customer get all customers.");
        List<CustomerResponseDTO> customers = customerService.findAll();
        logger.debug("Completed request to get customer get all customers.");
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @Operation(summary = "Update customer", description = "Updates customer information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerDTO customerDTO) {
        logger.debug("Processing request to update customer with id {}.", id);
        CustomerResponseDTO updated = customerService.update(id, customerDTO);
        logger.debug("Completed request to update customer with id {}.", id);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @Operation(summary = "Partially update customer", description = "Allows partial updates to customer data")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerResponseDTO>> updateCustomerPartially(
            @PathVariable String id,
            @Valid @RequestBody CustomerPatchDTO patchDTO) {
        logger.debug("Processing PATCH request to update customer with id {}.", id);
        CustomerResponseDTO updated = customerService.update(id, patchDTO);
        logger.debug("Completed PATCH request to update customer with id {}.", id);
        return ResponseEntity.ok(ApiResponse.success("Customer updated successfully", updated));
    }

    @Operation(summary = "Batch update customers", description = "Allows batch updates of customer data")
    @PatchMapping("/batch")
    public ResponseEntity<ApiResponse<CustomerBatchUpdateResponseDTO>> batchUpdateCustomers(
            @Valid @RequestBody CustomerBatchUpdateDTO batchUpdateDTO) {
        logger.debug("Processing request to update customers in batch.");
        CustomerBatchUpdateResponseDTO updatedCustomers = customerService.batchUpdate(batchUpdateDTO);
        logger.debug("Completed batch customers request.");
        return ResponseEntity.ok(ApiResponse.success("Batch operation executed successfully", updatedCustomers));
    }

    @Operation(summary = "Delete customer", description = "Deletes customer record")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable String id) {
        logger.debug("Processing request to delete customer with id {}.", id);
        customerService.delete(id);
        logger.debug("Completed request to delete customer with id {}.", id);
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> checkCustomerExists(@PathVariable String id) {
        logger.debug("Processing request to check if customer with id {} exists.", id);
        customerService.findById(id);
        logger.debug("Completed request to check if customer with id {} exists.", id);
        return ResponseEntity.noContent().build();
    }
}
