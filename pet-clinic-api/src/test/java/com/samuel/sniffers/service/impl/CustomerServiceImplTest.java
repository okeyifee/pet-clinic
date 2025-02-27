package com.samuel.sniffers.service.impl;

import com.samuel.sniffers.api.exception.CustomerAlreadyExistsException;
import com.samuel.sniffers.api.exception.InvalidRequestException;
import com.samuel.sniffers.api.exception.ResourceNotFoundException;
import com.samuel.sniffers.api.factory.EntityFactory;
import com.samuel.sniffers.api.response.PagedResponse;
import com.samuel.sniffers.dto.CustomerBatchUpdateDTO;
import com.samuel.sniffers.dto.CustomerDTO;
import com.samuel.sniffers.dto.CustomerPatchDTO;
import com.samuel.sniffers.dto.response.*;
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.security.SecurityFilter;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.CustomerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@Transactional // Resets the database to its state before the test
@ActiveProfiles("test")
class CustomerServiceImplTest extends BaseServiceTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerService customerService;

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

    @ParameterizedTest
    @DisplayName("create - Should create customer when it doesn't exist")
    @MethodSource("createCustomer")
    void testCreateCustomerWithValidToken_ShouldSucceed(String token) throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, token);

        // Act
        CustomerResponseDTO response = customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, token));

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotEmpty();
        assertThat(response.getCreated()).isInThePast();
    }

    @Test
    @DisplayName("create - Should throw exception when customer already exists")
    void createCustomerAlreadyExists() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create first customer
        customerService.create(customerDTO);

        // create second customer should trigger 409
        assertThatThrownBy(() -> customerService.create(customerDTO))
                .isInstanceOf(CustomerAlreadyExistsException.class);

        final long count = customerRepository.findAllWithAccess(TEST_ADMIN_TOKEN, false).size();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("findById - Should return customer when found")
    void findByIdCustomerFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        // Act
        CustomerResponseDTO result = customerService.findById(responseDTO.getId());

        // Assert
        validateCustomerMatch(responseDTO, result);
    }

    @Test
    @DisplayName("findById - Should throw exception when customer not found")
    void findByIdCustomerNotFound() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // create customer
        customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));

        final String inValidId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> customerService.findById(inValidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("findAll - Should return all customers for admin")
    void findAll_AsAdmin_ShouldReturnAllCustomers() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create multiple customers with unique names
        CustomerDTO customerDTO1 = getCustomerDTO("Unique Admin1", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO2 = getCustomerDTO("Unique Admin2", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO3 = getCustomerDTO("Unique Admin3", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        customerService.create(customerDTO1);
        customerService.create(customerDTO2);
        customerService.create(customerDTO3);

        // Act
        PagedResponse<CustomerResponseDTO> result = customerService.findAll(1, 10, "name", "asc", "/api/customers");

        // Assert
        assertThat(result.getData()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsExactly("Unique Admin1", "Unique Admin2", "Unique Admin3");

        // Additional assertions for PagedResponse
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isLast()).isTrue();
        assertThat(result.getLinks()).isNotNull();
        assertThat(result.getLinks()).hasSize(2);
    }

    @Test
    @DisplayName("findAll - Should return only customers for non-admin user")
    void findAll_AsNonAdmin_ShouldReturnOnlyUserCustomers() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO1 = getCustomerDTO("Unique Customer 3", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        customerService.create(customerDTO1);

        // Setup context with customer 2 token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_CUSTOMER2_TOKEN);
        CustomerDTO customerDTO2 = getCustomerDTO("Unique Customer 2", TEST_TIMEZONE_UTC, TEST_CUSTOMER2_TOKEN);
        customerService.create(customerDTO2);

        // Setup context with customer 1 token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_CUSTOMER1_TOKEN);
        CustomerDTO customerDTO3 = getCustomerDTO("Unique Customer 1", TEST_TIMEZONE_UTC, TEST_CUSTOMER1_TOKEN);
        customerService.create(customerDTO3);

        // Act
        PagedResponse<CustomerResponseDTO> result = customerService.findAll(1, 10, "name", "asc", "/api/customers");

        // Assert
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsOnly("Unique Customer 1");
    }

    @Test
    @DisplayName("findAll - Should handle pagination correctly")
    void findAll_ShouldHandlePagination() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create multiple customers with unique names
        for (int i = 1; i <= 5; i++) {
            CustomerDTO customerDTO = getCustomerDTO("Unique Admin " + i, TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
            customerService.create(customerDTO);
        }

        // Act - First page with 2 items
        PagedResponse<CustomerResponseDTO> resultPage1 = customerService.findAll(1, 2, "name", "asc", "/api/customers");

        // Assert first page
        assertThat(resultPage1.getData()).hasSize(2);
        assertThat(resultPage1.getTotalElements()).isEqualTo(5);
        assertThat(resultPage1.getTotalPages()).isEqualTo(3);
        assertThat(resultPage1.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsExactly("Unique Admin 1", "Unique Admin 2");
        assertThat(resultPage1.isLast()).isFalse();

        // Act - Second page with 2 items
        PagedResponse<CustomerResponseDTO> resultPage2 = customerService.findAll(2, 2, "name", "asc", "/api/customers");

        // Assert second page
        assertThat(resultPage2.getData()).hasSize(2);
        assertThat(resultPage2.getTotalElements()).isEqualTo(5);
        assertThat(resultPage2.getTotalPages()).isEqualTo(3);
        assertThat(resultPage2.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsExactly("Unique Admin 3", "Unique Admin 4");
    }

    @Test
    @DisplayName("findAll - Should return empty page when no customers exist")
    void findAll_ShouldReturnEmptyPageWhenNoCustomers() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        PagedResponse<CustomerResponseDTO> result = customerService.findAll(1, 10, "name", "asc", "/api/customers");

        // Assert
        assertThat(result.getData()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("findAll - Should handle sorting correctly")
    void findAll_ShouldHandleSorting() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Create customers in mixed order with unique names
        CustomerDTO customerDTO1 = getCustomerDTO("Z Unique Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO2 = getCustomerDTO("A Unique Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO3 = getCustomerDTO("M Unique Customer", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        customerService.create(customerDTO1);
        customerService.create(customerDTO2);
        customerService.create(customerDTO3);

        // Act - Ascending sort
        PagedResponse<CustomerResponseDTO> resultAsc = customerService.findAll(1, 10, "name", "asc", "/api/customers");

        // Assert ascending sort
        assertThat(resultAsc.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsExactly("A Unique Customer", "M Unique Customer", "Z Unique Customer");

        // Act - Descending sort
        PagedResponse<CustomerResponseDTO> resultDesc = customerService.findAll(1, 10, "name", "desc", "/api/customers");

        // Assert descending sort
        assertThat(resultDesc.getData())
                .extracting(CustomerResponseDTO::getName)
                .containsExactly("Z Unique Customer", "M Unique Customer", "A Unique Customer");
    }

    @Test
    @DisplayName("delete - Should delete customer when found")
    void deleteCustomer() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        // Validate customer is created
        long count = customerRepository.findAllWithAccess(TEST_ADMIN_TOKEN, false).size();
        assertThat(count).isEqualTo(1);

        // Act
        customerService.delete(responseDTO.getId());

        // Assert
        count = customerRepository.findAllWithAccess(TEST_ADMIN_TOKEN, false).size();
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("delete - Should throw exception when customer not found")
    void deleteCustomerNotFound() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // create customer
        customerService.create(getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN));

        final String inValidId = getUniqueUUID();

        // Act & Assert
        assertThatThrownBy(() -> customerService.delete(inValidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("customerExist - Should return true when customer exists")
    void customerExistTrue() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        // Validate customer is created
        long count = customerRepository.findAllWithAccess(TEST_ADMIN_TOKEN, false).size();
        assertThat(count).isEqualTo(1);

        // Act
        boolean result = customerService.customerExist(responseDTO.getId());

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("customerExist - Should return false when customer does not exist")
    void customerExistFalse() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        // Act
        boolean result = customerService.customerExist(getUniqueUUID());

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("update - Should update customer with DTO")
    void updateCustomerWithDTO() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer 1
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        CustomerDTO dto = new CustomerDTO();
        dto.setName("new name");
        dto.setTimezone(TEST_TIMEZONE_GMT);

        // Act
        CustomerResponseDTO result = customerService.update(responseDTO.getId(), dto);

        // Assert
        assertThat(result.getName()).isEqualTo("new name");
        assertThat(result.getTimezone()).isEqualTo("GMT");
    }

    @ParameterizedTest
    @DisplayName("update - Should update customer with PatchDTO")
    @MethodSource("getPatchDTO")
    void updateCustomerWithPatchDTO(CustomerPatchDTO dto) throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer 1
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        // Act
        CustomerResponseDTO result = customerService.update(responseDTO.getId(), dto);

        // Assert
        if (dto.getTimezone() !=  null) {
            assertThat(result.getTimezone()).isEqualTo(dto.getTimezone());
        } else {
            assertThat(result.getTimezone()).isEqualTo(responseDTO.getTimezone());
        }

        if (dto.getName() != null) {
            assertThat(result.getName()).isEqualTo(dto.getName());
        } else {
            assertThat(result.getName()).isEqualTo(responseDTO.getName());
        }
    }

    @Test
    @DisplayName("update - Should throw exception when PatchDTO is empty")
    void updateCustomerWithEmptyPatchDTO() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO = getCustomerDTO("Test Admin", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customer 1
        CustomerResponseDTO responseDTO = customerService.create(customerDTO);

        CustomerPatchDTO dto = new CustomerPatchDTO(); // Both name and timezone are null
        String customerId = responseDTO.getId();

        // Act & Assert
        assertThatThrownBy(() -> customerService.update(customerId, dto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("You must provide either 'name' or 'timezone' in the PATCH request. Both fields cannot be empty.");
    }
    
    @Test
    @DisplayName("batchUpdate - Should update multiple customers successfully")
    void batchUpdateCustomers() throws ServletException, IOException {

        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO1 = getCustomerDTO("Test Admin1", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO2 = getCustomerDTO("Test Admin2", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO3 = getCustomerDTO("Test Admin3", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customers
        CustomerResponseDTO responseDTO1 = customerService.create(customerDTO1);
        CustomerResponseDTO responseDTO2 = customerService.create(customerDTO2);
        CustomerResponseDTO responseDTO3 = customerService.create(customerDTO3);


        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch1 = getCustomerBatchUpdateDTO(responseDTO1.getId(), "Updated Name 1", null);
        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch2 = getCustomerBatchUpdateDTO(responseDTO2.getId(), null, TEST_TIMEZONE_GMT);
        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch3 = getCustomerBatchUpdateDTO(responseDTO3.getId(), "New name", TEST_TIMEZONE_GMT);

        CustomerBatchUpdateDTO dto = new CustomerBatchUpdateDTO();
        dto.setUpdates(List.of(patch1, patch2, patch3));

        // Act
        CustomerBatchUpdateResponseDTO result = customerService.batchUpdate(dto);

        // Assert
        assertThat(result.getSuccessfulUpdatesCount()).isEqualTo(dto.getUpdates().size());
        assertThat(result.getFailedUpdatesCount()).isNull();
        assertThat(result.getSuccessfulUpdates()).hasSize(dto.getUpdates().size());
        assertThat(result.getFailedUpdates()).isEmpty();
    }

    @Test
    @DisplayName("batchUpdate - Should handle failures in batch update")
    void batchUpdateCustomersWithFailures() throws ServletException, IOException {
        // Setup context with admin token
        setUpTestWithToken(securityFilter, mockFilterChain, TEST_ADMIN_TOKEN);

        CustomerDTO customerDTO1 = getCustomerDTO("Test Admin1", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO2 = getCustomerDTO("Test Admin2", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);
        CustomerDTO customerDTO3 = getCustomerDTO("Test Admin3", TEST_TIMEZONE_UTC, TEST_ADMIN_TOKEN);

        // create customers
        CustomerResponseDTO responseDTO1 = customerService.create(customerDTO1);
        customerService.create(customerDTO2);
        CustomerResponseDTO responseDTO3 = customerService.create(customerDTO3);

        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch1 = getCustomerBatchUpdateDTO(responseDTO1.getId(), "Updated Name 1", null);
        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch2 = getCustomerBatchUpdateDTO("InvalidId", null, TEST_TIMEZONE_GMT);
        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch3 = getCustomerBatchUpdateDTO(responseDTO3.getId(), null, null);

        CustomerBatchUpdateDTO dto = new CustomerBatchUpdateDTO();
        dto.setUpdates(List.of(patch1, patch2, patch3));

        // Act
        CustomerBatchUpdateResponseDTO result = customerService.batchUpdate(dto);

        // Assert
        assertThat(result.getSuccessfulUpdatesCount()).isEqualTo(1);
        assertThat(result.getFailedUpdatesCount()).isEqualTo(2);
        assertThat(result.getSuccessfulUpdates()).hasSize(1);
        assertThat(result.getFailedUpdates()).hasSize(2);

        Optional<BatchUpdateFailure> nonExistentFailure = result.getFailedUpdates().stream()
                .filter(f -> f.getId().equals("InvalidId"))
                .findFirst();

        assertThat(nonExistentFailure).isPresent();
        assertThat(nonExistentFailure.get().getError()).contains("Customer not found");

        Optional<BatchUpdateFailure> invalidRequestFailure = result.getFailedUpdates().stream()
                .filter(f -> f.getId().equals(responseDTO3.getId()))
                .findFirst();
        assertThat(invalidRequestFailure).isPresent();
        assertThat(invalidRequestFailure.get().getError()).contains("Both fields cannot be empty");
    }

    static Stream<Arguments> getPatchDTO() {

        CustomerPatchDTO dto1 = new CustomerPatchDTO();
        dto1.setName("new name");

        CustomerPatchDTO dto2 = new CustomerPatchDTO();
        dto2.setTimezone(TEST_TIMEZONE_GMT);

        return Stream.of(
                Arguments.of(dto1),
                Arguments.of(dto2)
        );
    }

    static Stream<Arguments> createCustomer() {
        return Stream.of(
                Arguments.of(TEST_ADMIN_TOKEN),
                Arguments.of(TEST_CUSTOMER1_TOKEN),
                Arguments.of(TEST_CUSTOMER2_TOKEN)
        );
    }

    private void validateCustomerMatch(CustomerResponseDTO expected, CustomerResponseDTO actual) {
        assertThat(actual.getCreated()).isEqualTo(expected.getCreated());
        assertThat(actual.getTimezone()).isEqualTo(expected.getTimezone());
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getId()).isEqualTo(expected.getId());

        validateBasketMatch(expected.getBaskets(), actual.getBaskets());
    }

    private void validateBasketMatch(List<BasketResponseDTO> expected, List<BasketResponseDTO> actual) {
        if (expected == null) {
            assertThat(actual).isNull();
            return;
        }

        if (expected.isEmpty()) {
            assertThat(actual).isEmpty();
            return;
        }

        assertThat(actual).hasSize(expected.size());

        // Match baskets by ID
        for (BasketResponseDTO expectedBasket : expected) {
            BasketResponseDTO actualBasket = actual.stream()
                    .filter(b -> b.getId().equals(expectedBasket.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Basket with ID " + expectedBasket.getId() + " not found"));

            assertThat(actualBasket.getStatus()).isEqualTo(expectedBasket.getStatus());
            assertThat(actualBasket.getStatusDate()).isEqualTo(expectedBasket.getStatusDate());
            assertThat(actualBasket.getCreated()).isEqualTo(expectedBasket.getCreated());

            validateItemMatch(expectedBasket.getItems(), actualBasket.getItems());
        }
    }

    private void validateItemMatch(Set<ItemResponseDTO> expected, Set<ItemResponseDTO> actual) {
        if (expected == null) {
            assertThat(actual).isNull();
            return;
        }

        if (expected.isEmpty()) {
            assertThat(actual).isEmpty();
            return;
        }

        assertThat(actual).hasSize(expected.size());

        // Match items by ID
        for (ItemResponseDTO expectedItem : expected) {
            ItemResponseDTO actualItem = actual.stream()
                    .filter(i -> i.getId().equals(expectedItem.getId()))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Item with ID " + expectedItem.getId() + " not found"));

            assertThat(actualItem.getDescription()).isEqualTo(expectedItem.getDescription());
            assertThat(actualItem.getAmount()).isEqualTo(expectedItem.getAmount());
        }
    }

    private static CustomerBatchUpdateDTO.CustomerBatchPatchDTO getCustomerBatchUpdateDTO(String id, String name, String timeZone) {
        CustomerBatchUpdateDTO.CustomerBatchPatchDTO patch = new CustomerBatchUpdateDTO.CustomerBatchPatchDTO();
        patch.setId(id);
        patch.setName(name);
        patch.setTimezone(timeZone);

        return patch;
    }
}