package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.InvalidRequestException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.dto.CustomerBatchUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.CustomerPatchDTO;
import com.samuel.sniffers.dto.response.BatchUpdateFailure;
import com.samuel.sniffers.dto.response.CustomerBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
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
public class CustomerServiceImpl implements CustomerService {

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CustomerRepository customerRepository;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    @Override
    public CustomerResponseDTO create(CustomerDTO dto) {
        Customer customer = entityFactory.convertToEntity(dto, Customer.class);
        customer.setOwnerToken(securityService.getCurrentCustomerToken());
        return entityFactory.convertToDTO(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Override
    public CustomerResponseDTO findById(String id) {
        return customerRepository.findByIdWithAccess(
                        id,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .map( customer -> entityFactory.convertToDTO(customer, CustomerResponseDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException(getFormattedNotFoundMessage(id)));
    }

    @Override
    public List<CustomerResponseDTO> findAll() {
        String token = securityService.getCurrentCustomerToken();
        return customerRepository.findAllWithAccess(token, securityService.isAdmin(token))
                .stream()
                .map(customer -> entityFactory.convertToDTO(customer, CustomerResponseDTO.class))
                .toList();
    }

    @Override
    public CustomerResponseDTO update(String id, CustomerDTO dto) {
        Customer customer = customerRepository.findByIdWithAccess(
                        id,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(getFormattedNotFoundMessage(id)));

        customer.setName(dto.getName());
        customer.setTimezone(dto.getTimezone());

        return entityFactory.convertToDTO(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Override
    public CustomerResponseDTO update(String id, CustomerPatchDTO dto) {

        if (dto.getName() == null && dto.getTimezone() == null) {
            throw new InvalidRequestException("You must provide either 'name' or 'timezone' in the PATCH request. Both fields cannot be empty.");
        }

        Customer customer = customerRepository.findByIdWithAccess(
                        id,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(getFormattedNotFoundMessage(id)));

        customer = entityFactory.patchEntity(dto, customer);
        return entityFactory.convertToDTO(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Override
    public CustomerBatchUpdateResponseDTO batchUpdate(CustomerBatchUpdateDTO dto) {

        final List<String> ids = dto.getUpdates().stream()
                .map(CustomerBatchUpdateDTO.CustomerBatchPatchDTO::getId)
                .toList();

        // Fetch all customers in a single query
        List<Customer> customers = customerRepository.findAllByIdAndOwnerToken(
                ids,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        );

        Map<String, Customer> customerMap = customers
                .stream()
                .collect(Collectors.toMap(Customer::getId, c -> c));

        List<Customer> updatedCustomers = new ArrayList<>();
        List<BatchUpdateFailure> failedUpdates = new ArrayList<>();

        for (CustomerBatchUpdateDTO.CustomerBatchPatchDTO updateRequest : dto.getUpdates()) {

            try {
                if (updateRequest.getName() == null && updateRequest.getTimezone() == null) {
                    throw new InvalidRequestException("You must provide either 'name' or 'timezone' in the PATCH request. Both fields cannot be empty.");
                }

                Customer customer = customerMap.get(updateRequest.getId());
                if (customer == null) { // Customer not found or owner token mismatch
                    failedUpdates.add(
                            new BatchUpdateFailure(
                                    updateRequest.getId(),
                                    "Customer not found.")
                    );
                } else {
                    // PATCH customer fields
                    customer = entityFactory.patchEntity(updateRequest, customer);
                    updatedCustomers.add(customer);
                }
            } catch (Exception ex) {
                final String formattedErrorResponse = String.format("Failed to update customer record: %s", ex.getMessage());
                logger.error(formattedErrorResponse, ex);
                failedUpdates.add(
                        new BatchUpdateFailure(
                                updateRequest.getId(),
                                "Failed to update customer record: " + ex.getMessage())
                );
            }
        }

        // Save all updates in a single batch operation
        customerRepository.saveAll(updatedCustomers);

        return new CustomerBatchUpdateResponseDTO(
                !updatedCustomers.isEmpty() ? updatedCustomers.size() : null,
                !failedUpdates.isEmpty() ? failedUpdates.size() : null,
                entityFactory.convertToEntityList(updatedCustomers, CustomerResponseDTO.class),
                failedUpdates
        );
    }

    @Override
    public void delete(String id) {
        Customer customer = customerRepository.findByIdWithAccess(
                        id,
                        securityService.getCurrentCustomerToken(),
                        securityService.isAdmin(securityService.getCurrentCustomerToken())
                )
                .orElseThrow(() -> new ResourceNotFoundException(getFormattedNotFoundMessage(id)));

        customerRepository.delete(customer);
    }

    private String getFormattedNotFoundMessage(String id) {
        logger.error("Customer with id: {} not found", id);
        return "Customer not found";
    }
}