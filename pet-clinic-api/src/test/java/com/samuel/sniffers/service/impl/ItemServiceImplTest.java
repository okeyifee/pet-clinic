package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.dto.response.ItemResponseDTO;
import com.samuel.sniffers.security.SecurityFilter;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
import com.samuel.sniffers.service.ItemService;
import com.samuel.sniffers.service.ShoppingBasketService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class ItemServiceImplTest extends BaseServiceTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ShoppingBasketService basketService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private EntityFactory entityFactory;

    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        mockFilterChain = mock(FilterChain.class);

        // Clear the RequestContextHolder after each test
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("create - Should create item in a customer's basket successfully")
    void testCreateItemWithValidToken() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerResponseDTO customerResponseDTO = createCustomer();
        BasketResponseDTO basketResponseDTO = createBasket(customerResponseDTO.getId());

        // Act
        ItemDTO itemDTO = getItemDTO("description", 20);
        ItemResponseDTO response = itemService.createItem(
                customerResponseDTO.getId(),
                basketResponseDTO.getId(),
                itemDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getAmount()).isEqualTo(itemDTO.getAmount());
        assertThat(response.getDescription()).isEqualTo(itemDTO.getDescription());

        //clean up
        customerService.delete(customerResponseDTO.getId());
    }

    @Test
    @DisplayName("createItem - Should throw exception when customer not found")
    void createItemCustomerNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);
        String uuid = getUniqueUUID();

        // Act
        ItemDTO itemDTO = getItemDTO("description", 20);


        // Act & Assert
        assertThatThrownBy(() -> itemService.createItem(uuid, uuid, itemDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("createItem - Should throw exception when basket not found")
    void createItemBasketNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        String customerId = createCustomer().getId();
        String basketId = getUniqueUUID();

        // Act
        ItemDTO itemDTO = getItemDTO("description", 20);

        // Act & Assert
        assertThatThrownBy(() -> itemService.createItem(customerId, basketId, itemDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found");

        //clean up
        customerService.delete(customerId);
    }

    @Test
    @DisplayName("getItem - Should return item when found")
    void getItemFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        String customerId = createCustomer().getId();
        BasketResponseDTO basketResponseDTO = createBasket(customerId);
        ItemDTO itemDTO = getItemDTO("description", 20);
        ItemResponseDTO itemResponseDTO = itemService.createItem(customerId, basketResponseDTO.getId(), itemDTO);

        // Act
        ItemResponseDTO item = itemService.getItem(customerId, basketResponseDTO.getId(), itemResponseDTO.getId());

        // Assert
        assertThat(item).isNotNull();
        assertThat(item.getId()).isNotEmpty();
        assertThat(item.getAmount()).isEqualTo(itemDTO.getAmount());
        assertThat(item.getDescription()).isEqualTo(itemDTO.getDescription());

        //clean up
        customerService.delete(customerId);
    }

    @Test
    @DisplayName("getItem - Should throw exception when customer not found")
    void getItemCustomerNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);
        String uuid = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItem(uuid, uuid, uuid))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    private CustomerResponseDTO createCustomer () {
        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer
        return customerService.create(customerDTO);
    }

    private BasketResponseDTO createBasket(String customerId) {
        return basketService.createBasket(customerId);
    }
}