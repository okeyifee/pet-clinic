package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchItemUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.ItemDTO;
import com.samuel.sniffers.dto.UpdateItemDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.dto.response.ItemBatchUpdateResponseDTO;
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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@Transactional
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
                .hasMessage("Basket not found or access denied");

        //clean up
        customerService.delete(customerId);
    }

    @Test
    @DisplayName("getItem - Should return item when found")
    void getItemShouldReturnItem() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // create customer
        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        String customerId =  customerService.create(customerDTO).getId();

        BasketResponseDTO basketResponseDTO = basketService.createBasket(customerId);

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
    void getItemThrowsCustomerNotFoundException() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);
        String uuid = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> itemService.getItem(uuid, uuid, uuid))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("update item - Should update item successfully")
    void testUpdateItem_ShouldSucceed() throws ServletException, IOException {
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponse = basketService.createBasket(customerResponse.getId());

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setAmount(10);
        itemDTO.setDescription("Test Item");

        ItemResponseDTO createdItem = itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO);

        UpdateItemDTO updateItemDTO = new UpdateItemDTO();
        updateItemDTO.setAmount(20);
        updateItemDTO.setDescription("Updated Test Item");
        ItemResponseDTO updatedItem = itemService.updateItem(customerResponse.getId(), basketResponse.getId(), createdItem.getId(), updateItemDTO);

        assertThat(updatedItem.getId()).isEqualTo(createdItem.getId());
        assertThat(updatedItem.getAmount()).isEqualTo(20);
        assertThat(updatedItem.getDescription()).isEqualTo("Updated Test Item");
    }

    @Test
    @DisplayName("batch update items - Should update items successfully")
    void testBatchUpdateItems_ShouldSucceed() throws ServletException, IOException {
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponse = basketService.createBasket(customerResponse.getId());

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setAmount(10);
        itemDTO.setDescription("Item 1");

        ItemDTO itemDTO2 = new ItemDTO();
        itemDTO2.setAmount(20);
        itemDTO2.setDescription("Item 2");

        ItemResponseDTO item1 = itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO);
        ItemResponseDTO item2 = itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO2);

        BatchItemUpdateDTO.ItemPatchDTO itemPatchDTO = new BatchItemUpdateDTO.ItemPatchDTO();
        itemPatchDTO.setItemId(item1.getId());
        itemPatchDTO.setDescription("Updated Item 1");
        itemPatchDTO.setAmount(15);

        BatchItemUpdateDTO.ItemPatchDTO itemPatchDTO1 = new BatchItemUpdateDTO.ItemPatchDTO();
        itemPatchDTO1.setItemId(item2.getId());
        itemPatchDTO1.setDescription("Updated Item 2");
        itemPatchDTO1.setAmount(25);

        BatchItemUpdateDTO batchUpdateDTO = new BatchItemUpdateDTO();
        batchUpdateDTO.setUpdates(new ArrayList<>());
        batchUpdateDTO.getUpdates().add(itemPatchDTO);
        batchUpdateDTO.getUpdates().add(itemPatchDTO1);

        ItemBatchUpdateResponseDTO batchResponse = itemService.batchUpdateItems(customerResponse.getId(), basketResponse.getId(), batchUpdateDTO);

        assertThat(batchResponse.getSuccessfulUpdatesCount()).isNull();
        assertThat(batchResponse.getFailedUpdates()).hasSize(2);
        assertThat(batchResponse.getSuccessfulUpdates()).isEmpty();
        assertThat(batchResponse.getFailedUpdatesCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("find all items - Should return paged items for basket")
    void testFindAll_ShouldReturnPagedItems() throws ServletException, IOException {
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponse = basketService.createBasket(customerResponse.getId());

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setAmount(10);
        itemDTO.setDescription("Item 1");

        ItemDTO itemDTO2 = new ItemDTO();
        itemDTO2.setAmount(20);
        itemDTO2.setDescription("Item 2");

        itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO);
        itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO2);

        PagedResponse<ItemResponseDTO> pagedResponse = itemService.findAll(customerResponse.getId(), basketResponse.getId(), 1, 10, "id", "asc", "baseUrl");

        assertThat(pagedResponse.getData()).hasSize(2);
        assertThat(pagedResponse.getTotalPages()).isEqualTo(1);
        assertThat(pagedResponse.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("stream all items - Should stream all items for basket")
    void testStreamAllToResponse_ShouldSucceed() throws ServletException, IOException {
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponse = basketService.createBasket(customerResponse.getId());

        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setAmount(10);
        itemDTO.setDescription("Item 1");

        ItemDTO itemDTO2 = new ItemDTO();
        itemDTO2.setAmount(20);
        itemDTO2.setDescription("Item 2");

        itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO);
        itemService.createItem(customerResponse.getId(), basketResponse.getId(), itemDTO2);

        MockHttpServletResponse response = new MockHttpServletResponse();
        itemService.streamAllToResponse(response.getOutputStream(), customerResponse.getId(), basketResponse.getId());

        assertThat(response.getContentAsString())
                .contains("\"description\":\"Item 1\"")
                .contains("\"description\":\"Item 2\"");
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