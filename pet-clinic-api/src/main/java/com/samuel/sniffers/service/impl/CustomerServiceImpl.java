package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.CustomerAlreadyExistsException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CustomerServiceImpl implements CustomerService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CustomerRepository customerRepository;
    private final SecurityService securityService;
    private final EntityFactory entityFactory;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository, SecurityService securityService, EntityFactory entityFactory) {
        this.customerRepository = customerRepository;
        this.securityService = securityService;
        this.entityFactory = entityFactory;
    }

    @Override
    public CustomerResponseDTO create(CustomerDTO dto) {
        final String currentCustomerToken = securityService.getCurrentCustomerToken();

        if (customerRepository.existByNameAndOwnerToken(
                dto.getName(),
                currentCustomerToken,
                securityService.isAdmin(currentCustomerToken))) {

            throw new CustomerAlreadyExistsException();
        }

        Customer customer = entityFactory.convertToEntity(dto, Customer.class);
        customer.setOwnerToken(currentCustomerToken);
        return entityFactory.convertToDTO(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Override
    public CustomerResponseDTO findById(String customerId) {
        return entityFactory.convertToDTO(getCustomer(customerId), CustomerResponseDTO.class);
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
    public CustomerResponseDTO update(String customerId, CustomerDTO dto) {
        Customer customer = getCustomer(customerId);
        customer.setName(dto.getName());
        customer.setTimezone(dto.getTimezone());

        return entityFactory.convertToDTO(customerRepository.save(customer), CustomerResponseDTO.class);
    }

    @Override
    public CustomerResponseDTO update(String customerId, CustomerPatchDTO dto) {

        if (dto.getName() == null && dto.getTimezone() == null) {
            throw new InvalidRequestException("You must provide either 'name' or 'timezone' in the PATCH request. Both fields cannot be empty.");
        }

        Customer customer = getCustomer(customerId);
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
    public void delete(String customerId) {
        customerRepository.delete(getCustomer(customerId));
        logger.error("Customer with id: {} deleted successfully", customerId);
    }

    @Override
    public Customer getCustomer(String customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findByIdWithAccess(
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken())
        );

        if (optionalCustomer.isEmpty()) {
            logger.error("Customer with id: {} not found", customerId);
            throw new ResourceNotFoundException("Customer not found");
        }
        return optionalCustomer.get();
    }

    @Override
    public boolean customerExist(String customerId) {
        return customerRepository.existByIdAndOwnerToken(
                customerId,
                securityService.getCurrentCustomerToken(),
                securityService.isAdmin(securityService.getCurrentCustomerToken()));
    }
}