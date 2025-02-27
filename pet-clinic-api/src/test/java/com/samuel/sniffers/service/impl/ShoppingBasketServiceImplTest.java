package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.BatchBasketUpdateDTO;
import com.samuel.sniffers.dto.UpdateBasketDTO;
import com.samuel.sniffers.dto.response.BasketBatchUpdateResponseDTO;
import com.samuel.sniffers.dto.response.BasketResponseDTO;
import com.samuel.sniffers.dto.response.CustomerResponseDTO;
import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.enums.BasketStatus;
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.repository.ShoppingBasketRepository;
import com.samuel.sniffers.security.SecurityFilter;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
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
@Transactional // Resets the database to its state before the test
@ActiveProfiles("test")
class ShoppingBasketServiceImplTest extends BaseServiceTest {

    @Autowired
    private ShoppingBasketRepository basketRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private EntityFactory entityFactory;

    @Autowired
    private ShoppingBasketServiceImpl shoppingBasketService;

    private Customer customer;
    private ShoppingBasket basket;

    @Autowired
    private SecurityFilter securityFilter;

    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() {
        mockFilterChain = mock(FilterChain.class);

        // Clear the RequestContextHolder after each test
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("create - Should create basket successfully")
    void testCreateBasketWithValidToken_ShouldSucceed() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponseDTO = shoppingBasketService.createBasket(response.getId());

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getCreated()).isInThePast();

        // Assert
        assertThat(basketResponseDTO).isNotNull();
        assertThat(basketResponseDTO.getId()).isNotEmpty();
        assertThat(basketResponseDTO.getStatus()).isEqualTo(BasketStatus.NEW);
    }

    @Test
    @DisplayName("create - Should return 404 exception when customer with Id not found")
    void testCreateBasketInvalidCustomerId_ShouldReturnNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        String nonExistentCustomerId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.createBasket(nonExistentCustomerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("get basket - Should return basket when given correct customerId and basketId")
    void testGetBasket_ShouldReturnBasket() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponseDTO = shoppingBasketService.createBasket(response.getId());
        String customerId = response.getId();
        String basketId = basketResponseDTO.getId();

        BasketResponseDTO getBasketResponseDTO = shoppingBasketService.getBasket(customerId, basketId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getCreated()).isInThePast();

        // Assert
        assertThat(getBasketResponseDTO).isNotNull();
        assertThat(getBasketResponseDTO.getId()).isNotEmpty();
        assertThat(getBasketResponseDTO.getStatus()).isEqualTo(BasketStatus.NEW);
    }

    @Test
    @DisplayName("get basket - Should return 404 exception when customer with Id not found for Get Basket")
    void testGetBasketInvalidCustomerId_ShouldReturnNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        String nonExistentId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.getBasket(nonExistentId, nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("get basket - Should return 404 exception when basket with Id not found for Get Basket")
    void testGetBasketInvalidBasketId_ShouldReturnNotFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        String customerId = response.getId();
        String nonExistentBasketId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.getBasket(customerId, nonExistentBasketId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found or access denied");
    }

    @Test
    @DisplayName("delete basket - Should delete basket successfully")
    void testDeleteBasket_ShouldSucceed() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and basket
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        String customerId = customerResponse.getId();
        BasketResponseDTO basketResponse = shoppingBasketService.createBasket(customerId);
        String basketResponseId = basketResponse.getId();

        // Assert basket exist before delete
        BasketResponseDTO getBasketResponseDTO = shoppingBasketService.getBasket(customerId, basketResponseId);
        assertThat(getBasketResponseDTO).isNotNull();
        assertThat(getBasketResponseDTO.getId()).isNotEmpty();
        assertThat(getBasketResponseDTO.getStatus()).isEqualTo(BasketStatus.NEW);

        // Delete the basket
        shoppingBasketService.deleteBasket(customerId, basketResponseId);

        // Verify basket was deleted
        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.getBasket(customerId, basketResponseId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found or access denied");
    }

    @Test
    @DisplayName("delete basket - Should throw 404 Customer not found")
    void testDeleteBasket_throwCustomerNotFound_whenCustomerIdIsInvalid() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        String nonExistentId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.deleteBasket(nonExistentId, nonExistentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("delete basket - Should delete basket successfully")
    void testDeleteBasket_throwBasketNotFound_whenBasketIdIsInvalid() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        String customerId = response.getId();
        String nonExistentBasketId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.deleteBasket(customerId, nonExistentBasketId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found or access denied");
    }

    @Test
    @DisplayName("update basket - Should update basket status successfully")
    void testUpdateBasket_ShouldSucceed() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and basket
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponse = shoppingBasketService.createBasket(customerResponse.getId());

        // Update basket status
        UpdateBasketDTO updateBasketDTO = new UpdateBasketDTO();
        updateBasketDTO.setStatus(BasketStatus.PAID);

        BasketResponseDTO updatedResponse = shoppingBasketService.updateBasket(customerResponse.getId(), basketResponse.getId(), updateBasketDTO);

        // Verify updated status
        assertThat(updatedResponse.getStatus()).isEqualTo(BasketStatus.PAID);
    }

    @Test
    @DisplayName("update basket - Should throw customer not found (404) with Invalid customer Id")
    void testUpdateBasket_ShouldThrowCustomerNotFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        String nonExistentId = getUniqueUUID();

        UpdateBasketDTO updateBasketDTO = new UpdateBasketDTO();
        updateBasketDTO.setStatus(BasketStatus.PAID);

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.updateBasket(nonExistentId, nonExistentId, updateBasketDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("update basket - Should throw basket not found (404) with Invalid basket Id")
    void testUpdateBasket_ShouldThrowBasketNotFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        String customerId = response.getId();
        String nonExistentBasketId = getUniqueUUID();

        UpdateBasketDTO updateBasketDTO = new UpdateBasketDTO();
        updateBasketDTO.setStatus(BasketStatus.PAID);

        // Act & Assert
        assertThatThrownBy(() -> shoppingBasketService.updateBasket(customerId, nonExistentBasketId, updateBasketDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Basket not found or access denied");
    }

    @Test
    @DisplayName("find all baskets - Should return paged baskets for customer")
    void testFindAll_ShouldReturnPagedBaskets() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and baskets
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        shoppingBasketService.createBasket(customerResponse.getId());
        shoppingBasketService.createBasket(customerResponse.getId());

        // Find all baskets for customer
        PagedResponse<BasketResponseDTO> pagedResponse = shoppingBasketService.findAll(customerResponse.getId(), 1, 10, "id", "asc", "baseUrl");

        // Verify paged results
        assertThat(pagedResponse.getData()).hasSize(2);
        assertThat(pagedResponse.getTotalPages()).isEqualTo(1);
        assertThat(pagedResponse.getTotalElements()).isEqualTo(2L);
    }

    @Test
    @DisplayName("stream all baskets - Should stream all baskets for customer")
    void testStreamAllToResponse_ShouldSucceedWithBaskets() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and baskets
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basketResponseDTO = shoppingBasketService.createBasket(customerResponse.getId());
        BasketResponseDTO basketResponseDTO2 = shoppingBasketService.createBasket(customerResponse.getId());

        // Stream all baskets for customer
        MockHttpServletResponse response = new MockHttpServletResponse();
        shoppingBasketService.streamAllToResponse(response.getOutputStream(), customerResponse.getId());

        // Verify streamed JSON content
        assertThat(response.getContentAsString())
                .contains("\"status\":\"NEW\"")
                .contains("\"id\":\"" + basketResponseDTO.getId() + "\"");

        // Verify streamed JSON content
        assertThat(response.getContentAsString())
                .contains("\"status\":\"NEW\"")
                .contains("\"id\":\"" + basketResponseDTO2.getId() + "\"");
    }

    @Test
    @DisplayName("stream all baskets - Should return empty basket")
    void testStreamAllToResponse_ShouldSucceed() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and baskets
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));

        // Stream all baskets for customer
        MockHttpServletResponse response = new MockHttpServletResponse();
        shoppingBasketService.streamAllToResponse(response.getOutputStream(), customerResponse.getId());

        // Verify streamed JSON content
        assertThat(response.getContentAsString()).isEmpty();
    }

    @Test
    @DisplayName("stream all baskets - Should throw customer not found when invalid customer Id")
    void testStreamAllToResponse_ShouldThrowCustomerNotFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act & Assert
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> shoppingBasketService.streamAllToResponse(response.getOutputStream(), getUniqueUUID()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("batch update basket - Should batch update baskets successfully")
    void testBatchUpdateBasket_ShouldSucceed() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and baskets
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basket1Response = shoppingBasketService.createBasket(customerResponse.getId());
        BasketResponseDTO basket2Response = shoppingBasketService.createBasket(customerResponse.getId());

        // Update basket2 status
        UpdateBasketDTO updateBasketDTO = new UpdateBasketDTO();
        updateBasketDTO.setStatus(BasketStatus.PAID);
        shoppingBasketService.updateBasket(customerResponse.getId(), basket2Response.getId(), updateBasketDTO);

        // Batch update basket statuses
        BatchBasketUpdateDTO batchUpdateDTO = new BatchBasketUpdateDTO();
        batchUpdateDTO.setUpdates(new ArrayList<>());

        BatchBasketUpdateDTO.BasketPatchDTO basketPatchDTO = new BatchBasketUpdateDTO.BasketPatchDTO();
        basketPatchDTO.setStatus(BasketStatus.PAID);
        basketPatchDTO.setBasketId(basket1Response.getId());
        batchUpdateDTO.getUpdates().add(basketPatchDTO);

        BatchBasketUpdateDTO.BasketPatchDTO basketPatchDTO1 = new BatchBasketUpdateDTO.BasketPatchDTO();
        basketPatchDTO1.setStatus(BasketStatus.PROCESSED);
        basketPatchDTO1.setBasketId(basket2Response.getId());
        batchUpdateDTO.getUpdates().add(basketPatchDTO1);

        BasketBatchUpdateResponseDTO batchResponse = shoppingBasketService.batchUpdateBasket(customerResponse.getId(), batchUpdateDTO);

        // Verify batch update results
        assertThat(batchResponse.getSuccessfulUpdatesCount()).isEqualTo(2);
        assertThat(batchResponse.getFailedUpdates()).isEmpty();
        assertThat(batchResponse.getSuccessfulUpdates())
                .extracting(BasketResponseDTO::getStatus)
                .containsExactlyInAnyOrder(BasketStatus.PAID, BasketStatus.PROCESSED);
    }

    @Test
    @DisplayName("batch update basket - Should batch update success and fail batch")
    void testBatchUpdateBasket_PartialSucceed() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customer and baskets
        CustomerResponseDTO customerResponse = customerService.create(getCustomerDTO("Test Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));
        BasketResponseDTO basket1Response = shoppingBasketService.createBasket(customerResponse.getId());
        shoppingBasketService.createBasket(customerResponse.getId());

        // Batch update basket statuses
        BatchBasketUpdateDTO batchUpdateDTO = new BatchBasketUpdateDTO();
        batchUpdateDTO.setUpdates(new ArrayList<>());

        BatchBasketUpdateDTO.BasketPatchDTO basketPatchDTO = new BatchBasketUpdateDTO.BasketPatchDTO();
        basketPatchDTO.setStatus(BasketStatus.PAID);
        basketPatchDTO.setBasketId(basket1Response.getId());
        batchUpdateDTO.getUpdates().add(basketPatchDTO);

        BatchBasketUpdateDTO.BasketPatchDTO basketPatchDTO1 = new BatchBasketUpdateDTO.BasketPatchDTO();
        batchUpdateDTO.getUpdates().add(basketPatchDTO1);

        BasketBatchUpdateResponseDTO batchResponse = shoppingBasketService.batchUpdateBasket(customerResponse.getId(), batchUpdateDTO);

        // Verify batch update results
        assertThat(batchResponse.getSuccessfulUpdatesCount()).isEqualTo(1);
        assertThat(batchResponse.getFailedUpdates()).hasSize(1);
        assertThat(batchResponse.getSuccessfulUpdates())
                .extracting(BasketResponseDTO::getStatus)
                .containsExactlyInAnyOrder(BasketStatus.PAID);
    }
}

