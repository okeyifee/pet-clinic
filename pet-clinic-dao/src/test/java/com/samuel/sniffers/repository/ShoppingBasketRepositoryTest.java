package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.enums.BasketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ItemRepositoryTest.TestConfig.class)
@Transactional
class ShoppingBasketRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.samuel.sniffers.entity")
    @EnableJpaRepositories(basePackages = "com.samuel.sniffers.repository")
    static class TestConfig {
        // Empty configuration class that enables Spring Boot auto-configuration
    }

    @Autowired
    private ShoppingBasketRepository basketRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private final String OWNER_TOKEN_1 = "token1";
    private final String OWNER_TOKEN_2 = "token2";
    private final boolean IS_ADMIN = true;
    private final boolean NOT_ADMIN = false;

    private Customer customer1;
    private Customer customer2;
    private ShoppingBasket basket1;
    private ShoppingBasket basket2;
    private ShoppingBasket basket3;

    @BeforeEach
    void setUp() {
        // Clean db records before starting
        basketRepository.deleteAll();
        customerRepository.deleteAll();

        // Create customers
        customer1 = new Customer();
        customer1.setName("Test Customer 1");
        customer1.setTimezone("UTC");
        customer1.setOwnerToken(OWNER_TOKEN_1);
        customer1.setBaskets(new HashSet<>());

        customer2 = new Customer();
        customer2.setName("Test Customer 2");
        customer2.setTimezone("UTC");
        customer2.setOwnerToken(OWNER_TOKEN_2);
        customer2.setBaskets(new HashSet<>());

        customerRepository.saveAll(List.of(customer1, customer2));

        // Create baskets
        basket1 = new ShoppingBasket();
        basket1.setCustomer(customer1);
        basket1.setStatus(BasketStatus.NEW);
        basket1.setItems(new HashSet<>());

        basket2 = new ShoppingBasket();
        basket2.setCustomer(customer1);
        basket2.setStatus(BasketStatus.PAID);
        basket2.setItems(new HashSet<>());

        basket3 = new ShoppingBasket();
        basket3.setCustomer(customer2);
        basket3.setStatus(BasketStatus.NEW);
        basket3.setItems(new HashSet<>());

        basketRepository.saveAll(List.of(basket1, basket2, basket3));
    }

    @Test
    @DisplayName("streamAllWithAccess should return all baskets for customer as admin")
    void streamAllWithAccess_AsAdmin_ShouldReturnAllBasketsForCustomer() {
        // When: Streaming baskets for customer1 as admin
        try (Stream<ShoppingBasket> basketStream = basketRepository.streamAllWithAccess(
                customer1.getId(), "any-token", IS_ADMIN)) {
            // Then: Should return all baskets for customer1
            List<ShoppingBasket> baskets = basketStream.toList();
            assertThat(baskets).hasSize(2);
            assertThat(baskets).extracting(ShoppingBasket::getId)
                    .containsExactlyInAnyOrder(basket1.getId(), basket2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return baskets for matching token")
    void streamAllWithAccess_WithMatchingToken_ShouldReturnBaskets() {
        // When: Streaming baskets for customer1 with matching token
        try (Stream<ShoppingBasket> basketStream = basketRepository.streamAllWithAccess(
                customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN)) {
            // Then: Should return baskets for customer1
            List<ShoppingBasket> baskets = basketStream.toList();
            assertThat(baskets).hasSize(2);
            assertThat(baskets).extracting(ShoppingBasket::getId)
                    .containsExactlyInAnyOrder(basket1.getId(), basket2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for non-matching token")
    void streamAllWithAccess_WithNonMatchingToken_ShouldReturnEmptyStream() {
        // When: Streaming baskets for customer1 with token2
        try (Stream<ShoppingBasket> basketStream = basketRepository.streamAllWithAccess(
                customer1.getId(), OWNER_TOKEN_2, NOT_ADMIN)) {
            // Then: Should return empty list (no access)
            List<ShoppingBasket> baskets = basketStream.toList();
            assertThat(baskets).isEmpty();
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for invalid customer ID")
    void streamAllWithAccess_WithInvalidCustomerId_ShouldReturnEmptyStream() {
        // When: Streaming baskets for non-existent customer
        try (Stream<ShoppingBasket> basketStream = basketRepository.streamAllWithAccess(
                "non-existent-id", OWNER_TOKEN_1, NOT_ADMIN)) {
            // Then: Should return empty list
            List<ShoppingBasket> baskets = basketStream.toList();
            assertThat(baskets).isEmpty();
        }
    }

    @Test
    @DisplayName("findByCustomerWithAccess should return all baskets for customer as admin")
    void findByCustomerWithAccess_AsAdmin_ShouldReturnAllBasketsForCustomer() {
        // When: Finding baskets for customer1 as admin
        List<ShoppingBasket> baskets = basketRepository.findByCustomerWithAccess(
                customer1.getId(), "any-token", IS_ADMIN);

        // Then: Should return all baskets for customer1
        assertThat(baskets).hasSize(2);
        assertThat(baskets).extracting(ShoppingBasket::getId)
                .containsExactlyInAnyOrder(basket1.getId(), basket2.getId());
    }

    @Test
    @DisplayName("findByCustomerWithAccess should return baskets for matching token")
    void findByCustomerWithAccess_WithMatchingToken_ShouldReturnBaskets() {
        // When: Finding baskets for customer1 with matching token
        List<ShoppingBasket> baskets = basketRepository.findByCustomerWithAccess(
                customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN);

        // Then: Should return baskets for customer1
        assertThat(baskets).hasSize(2);
        assertThat(baskets).extracting(ShoppingBasket::getId)
                .containsExactlyInAnyOrder(basket1.getId(), basket2.getId());
    }
}