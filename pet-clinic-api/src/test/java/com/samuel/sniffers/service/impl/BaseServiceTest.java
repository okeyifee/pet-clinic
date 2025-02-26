package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.constants.SecurityConstants;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.security.SecurityFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public abstract class BaseServiceTest {

    protected static final String TEST_TIMEZONE_UTC = "UTC";
    protected static final String TEST_TIMEZONE_GMT = "GMT";
    protected static final String TEST_ADMIN_TOKEN = "test-admin";
    protected static final String TEST_CUSTOMER1_TOKEN = "test-customer1";
    protected static final String TEST_CUSTOMER2_TOKEN = "test-customer2";
    protected static final String TEST_INVALID_CUSTOMER_TOKEN = "test-invalid";

    private Customer customer;
    private CustomerDTO customerDTO;
    private ShoppingBasket basket;

    protected Customer getAdminCustomer() {
        return getCustomer("Test Admin", TEST_TIMEZONE_GMT, TEST_ADMIN_TOKEN);
    }

    protected Customer getCustomer1() {
        return getCustomer("Test Customer1", TEST_TIMEZONE_UTC, TEST_CUSTOMER1_TOKEN);
    }

    protected Customer getCustomer2() {
        return getCustomer("Test Customer2", TEST_TIMEZONE_UTC, TEST_CUSTOMER2_TOKEN);
    }

    protected Customer getCustomerWithInvalidToken() {
        return getCustomer("Test Customer3", TEST_TIMEZONE_GMT, TEST_INVALID_CUSTOMER_TOKEN);
    }

    protected Customer getCustomer(String name, String timezone, String token) {
        customer = new Customer();
        customer.setName(name);
        customer.setTimezone(timezone);
        customer.setCreated(LocalDateTime.now());
        customer.setOwnerToken(token);

        return customer;
    }

    protected CustomerDTO getCustomerDTO(String name, String timezone, String token) {
        customerDTO = new CustomerDTO();
        customerDTO.setName(name);
        customerDTO.setTimezone(timezone);

        return customerDTO;
    }

//
//    private void initializeTestEntities() {}
//    private void initializeTestEntities() {}
//    private void initializeTestEntities() {}
//
//    private void initializeTestEntities() {
//
//
//        // Persist the customer
//        customer = customerRepository.save(customer);
//
//        // Verify the customer was saved correctly
//        Optional<Customer> savedCustomer = customerRepository.findByIdWithAccess(customer.getId(), "ADMIN_TOKEN_123", true);
//        assertThat(savedCustomer).isPresent();
//
//        // Use the saved customer for creating the basket
//        customer = savedCustomer.get();
//
//        basket = new ShoppingBasket();
//        basket.setId(UUID.randomUUID().toString());
//        basket.setCustomer(customer);
//        basket.setStatus(BasketStatus.NEW);
//        basket.setStatusDate(LocalDateTime.now());
//        basket.setCreated(LocalDateTime.now());
//
//        // Persist the basket
//        basketRepository.save(basket);
//    }



    protected void setUpTestWithToken(SecurityFilter filter, FilterChain mockFilterChain, String token) throws ServletException, IOException {
        // Set up mock HTTP request with auth header
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader(SecurityConstants.HEADER_AUTH,
                SecurityConstants.TOKEN_PREFIX + token);
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        // Set the RequestContextHolder with our mock request
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockRequest, mockResponse));
        filter.doFilter(mockRequest, mockResponse, mockFilterChain);
    }

    String getUniqueUUID() {
        return UUID.randomUUID().toString();
    }
}
