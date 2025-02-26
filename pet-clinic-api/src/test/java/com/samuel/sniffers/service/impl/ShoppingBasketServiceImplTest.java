//package com.samuel.sniffers.service.impl;
//
//import com.samuel.sniffers.api.constants.SecurityConstants;
//import com.samuel.sniffers.api.factory.EntityFactory;
//import com.samuel.sniffers.dto.response.BasketResponseDTO;
//import com.samuel.sniffers.entity.Customer;
//import com.samuel.sniffers.entity.ShoppingBasket;
//import com.samuel.sniffers.enums.BasketStatus;
//import com.samuel.sniffers.repository.CustomerRepository;
//import com.samuel.sniffers.repository.ShoppingBasketRepository;
//import com.samuel.sniffers.security.SecurityService;
//import com.samuel.sniffers.service.CustomerService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.context.request.RequestContextHolder;
//import org.springframework.web.context.request.ServletRequestAttributes;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//import java.util.UUID;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@ExtendWith(SpringExtension.class)
////@Transactional // This ensures the test database is rolled back after each test
//@ActiveProfiles("test")
//class ShoppingBasketServiceImplTest {
//
//    @Autowired
//    private ShoppingBasketRepository basketRepository;
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Autowired
//    private CustomerService customerService;
//
//    @Autowired
//    private SecurityService securityService; // Use the real security service
//
//    @Autowired
//    private EntityFactory entityFactory;
//
//    @Autowired
//    private ShoppingBasketServiceImpl shoppingBasketService;
//
//    private Customer customer;
//    private ShoppingBasket basket;
//
//    @BeforeEach
//    void setUp() {
//
//        // Initialize entities before each test
//        initializeTestEntities();
//    }
//
//    @Test
//    void createBasket_ShouldCreateBasket() {
//
//        final String customerId = getUniqueCustomerId();
//
//        // Act
//        BasketResponseDTO response = shoppingBasketService.createBasket(customerId);
//
//        // Assert
//        assertThat(response).isNotNull();
//        assertThat(response.getId()).isNotEmpty();
//    }
//
//
//
////    @Test
////    void getBasket_ShouldReturnBasket() {
////        // Act
////        BasketResponseDTO response = shoppingBasketService.getBasket("customer1", "basket1");
////
////        // Assert
////        assertThat(response).isNotNull();
////        assertThat(response.getId()).isEqualTo("basket1");
////    }
////
////    @Test
////    void getAllBaskets_ShouldReturnListOfBaskets() {
////        // Act
////        List<BasketResponseDTO> responses = shoppingBasketService.getAllBaskets("customer1");
////
////        // Assert
////        assertThat(responses).isNotEmpty();
////        assertThat(responses.size()).isEqualTo(1);
////    }
////
////    @Test
////    void updateBasket_ShouldUpdateBasketStatus() {
////        // Arrange
////        UpdateBasketDTO updateDTO = new UpdateBasketDTO();
////        updateDTO.setStatus(BasketStatus.PAID);
////
////        // Act
////        BasketResponseDTO response = shoppingBasketService.updateBasket("customer1", "basket1", updateDTO);
////
////        // Assert
////        assertThat(response).isNotNull();
////        assertThat(basket.getStatus()).isEqualTo(BasketStatus.PAID);
////    }
////
////    @Test
////    void batchUpdateBasket_ShouldReturnBatchUpdateResponse() {
////        // Arrange
////        BatchBasketUpdateDTO updateDTO = new BatchBasketUpdateDTO();
////        BatchBasketUpdateDTO.BasketPatchDTO patchDTO = new BatchBasketUpdateDTO.BasketPatchDTO();
////        patchDTO.setBasketId("basket1");
////        patchDTO.setStatus(BasketStatus.PAID);
////        updateDTO.setUpdates(List.of(patchDTO));
////
////        // Act
////        BasketBatchUpdateResponseDTO response = shoppingBasketService.batchUpdateBasket("customer1", updateDTO);
////
////        // Assert
////        assertThat(response).isNotNull();
////        assertThat(response.getSuccessfulUpdatesCount()).isEqualTo(1);
////    }
////
////    @Test
////    void deleteBasket_ShouldDeleteBasket() {
////        // Act
////        shoppingBasketService.deleteBasket("customer1", "basket1");
////
////        // Assert
////        assertThat(basketRepository.findById("basket1")).isEmpty();
////    }
////
////    @Test
////    void validateCustomerExists_ShouldThrowResourceNotFoundException_WhenCustomerDoesNotExist() {
////        // Act & Assert
////        assertThatThrownBy(() -> shoppingBasketService.getBasket("customer2", "basket1"))
////                .isInstanceOf(ResourceNotFoundException.class)
////                .hasMessageContaining("Customer not found");
////    }
//
//
//    private void initializeTestEntities() {
//        customer = new Customer();
//        customer.setName("Test Customer");
//        customer.setTimezone("UTC");
//        customer.setCreated(LocalDateTime.now());
//        customer.setOwnerToken("ADMIN_TOKEN_123");
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
//}
//
