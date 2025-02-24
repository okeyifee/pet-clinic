package com.samuel.sniffers.service;

import com.samuel.sniffers.dto.CustomerBatchUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.CustomerPatchDTO;
import com.samuel.sniffers.dto.response.CustomerBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.entity.Customer;

import java.util.List;

public interface CustomerService {

    CustomerResponseDTO create(CustomerDTO dto);

    CustomerResponseDTO findById(String id);

    List<CustomerResponseDTO> findAll();

    CustomerResponseDTO update(String id, CustomerDTO dto);

    CustomerResponseDTO update(String id, CustomerPatchDTO dto);

    CustomerBatchUpdateResponseDTO batchUpdate(CustomerBatchUpdateDTO dto);

    void delete(String id);

    Customer getCustomer(String customerId);

    boolean customerExist(String customerId);
}
