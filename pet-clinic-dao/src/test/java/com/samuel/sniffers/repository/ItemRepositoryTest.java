package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.Item;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ItemRepositoryTest.TestConfig.class)
@Transactional
class ItemRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.samuel.sniffers.entity")
    @EnableJpaRepositories(basePackages = "com.samuel.sniffers.repository")
    static class TestConfig {
        // Empty configuration class that enables Spring Boot auto-configuration
    }

    @Autowired
    private ItemRepository itemRepository;

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
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        itemRepository.deleteAll();
        basketRepository.deleteAll();
        customerRepository.deleteAll();

        // Create and save test customers
        customer1 = new Customer();
        customer1.setName("Test Customer 1");
        customer1.setTimezone("UTC");
        customer1.setCreated(LocalDateTime.now());
        customer1.setOwnerToken(OWNER_TOKEN_1);
        customer1.setBaskets(new HashSet<>());

        customer2 = new Customer();
        customer2.setName("Test Customer 2");
        customer2.setTimezone("UTC");
        customer2.setCreated(LocalDateTime.now());
        customer2.setOwnerToken(OWNER_TOKEN_2);
        customer2.setBaskets(new HashSet<>());

        customerRepository.saveAll(List.of(customer1, customer2));

        // Create and save test baskets
        basket1 = new ShoppingBasket();
        basket1.setCustomer(customer1);
        basket1.setStatus(BasketStatus.NEW);
        basket1.setItems(new HashSet<>());

        basket2 = new ShoppingBasket();
        basket2.setCustomer(customer2);
        basket2.setStatus(BasketStatus.NEW);
        basket2.setItems(new HashSet<>());

        basketRepository.saveAll(List.of(basket1, basket2));

        // Create and save test items
        item1 = new Item();
        item1.setDescription("Test Item 1");
        item1.setAmount(10);
        item1.setBasket(basket1);

        item2 = new Item();
        item2.setDescription("Test Item 2");
        item2.setAmount(20);
        item2.setBasket(basket1);

        item3 = new Item();
        item3.setDescription("Test Item 3");
        item3.setAmount(30);
        item3.setBasket(basket2);

        itemRepository.saveAll(List.of(item1, item2, item3));
    }

    @Test
    @DisplayName("streamAllWithAccess should return all items for basket as admin")
    void streamAllWithAccess_AsAdmin_ShouldReturnAllItemsForBasket() {
        // When: Streaming items for basket1 as admin
        try (Stream<Item> itemStream = itemRepository.streamAllWithAccess(
                basket1.getId(), customer1.getId(), "any-token", IS_ADMIN)) {

            // Return all items in basket1
            List<Item> items = itemStream.toList();
            assertThat(items).hasSize(2);
            assertThat(items).extracting(Item::getId)
                    .containsExactlyInAnyOrder(item1.getId(), item2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return items for matching token")
    void streamAllWithAccess_WithMatchingToken_ShouldReturnItems() {
        // When: Streaming items for basket1 with matching token
        try (Stream<Item> itemStream = itemRepository.streamAllWithAccess(
                basket1.getId(), customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN)) {

            // Return items in basket1
            List<Item> items = itemStream.toList();
            assertThat(items).hasSize(2);
            assertThat(items).extracting(Item::getId)
                    .containsExactlyInAnyOrder(item1.getId(), item2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for non-matching token")
    void streamAllWithAccess_WithNonMatchingToken_ShouldReturnEmptyStream() {
        // When: Streaming items for basket1 with token2
        try (Stream<Item> itemStream = itemRepository.streamAllWithAccess(
                basket1.getId(), customer1.getId(), OWNER_TOKEN_2, NOT_ADMIN)) {
            // Then: Should return empty list (no access)
            List<Item> items = itemStream.toList();
            assertThat(items).isEmpty();
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for invalid basket ID")
    void streamAllWithAccess_WithInvalidBasketId_ShouldReturnEmptyStream() {
        // When: Streaming items for non-existent basket
        try (Stream<Item> itemStream = itemRepository.streamAllWithAccess(
                "non-existent-id", customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN)) {
            // Then: Should return empty list
            List<Item> items = itemStream.toList();
            assertThat(items).isEmpty();
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for invalid customer ID")
    void streamAllWithAccess_WithInvalidCustomerId_ShouldReturnEmptyStream() {
        // When: Streaming items for non-existent customer
        try (Stream<Item> itemStream = itemRepository.streamAllWithAccess(
                basket1.getId(), "non-existent-id", OWNER_TOKEN_1, NOT_ADMIN)) {
            // Then: Should return empty list
            List<Item> items = itemStream.toList();
            assertThat(items).isEmpty();
        }
    }

    @Test
    @DisplayName("findByCustomerWithAccess should return all items for basket as admin")
    void findByCustomerWithAccess_AsAdmin_ShouldReturnAllItemsForBasket() {
        // When: Finding items for basket1 as admin with pagination
        Page<Item> itemPage = itemRepository.findByCustomerWithAccess(
                basket1.getId(), customer1.getId(), "any-token", IS_ADMIN, Pageable.unpaged());

        // Then: Should return all items in basket1
        assertThat(itemPage.getContent()).hasSize(2);
        assertThat(itemPage.getContent()).extracting(Item::getId)
                .containsExactlyInAnyOrder(item1.getId(), item2.getId());
        assertThat(itemPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByCustomerWithAccess should return items for matching token")
    void findByCustomerWithAccess_WithMatchingToken_ShouldReturnItems() {
        // When: Finding items for basket1 with matching token and pagination
        Page<Item> itemPage = itemRepository.findByCustomerWithAccess(
                basket1.getId(), customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN, Pageable.unpaged());

        // Then: Should return items in basket1
        assertThat(itemPage.getContent()).hasSize(2);
        assertThat(itemPage.getContent()).extracting(Item::getId)
                .containsExactlyInAnyOrder(item1.getId(), item2.getId());
        assertThat(itemPage.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByCustomerWithAccess should return empty page for non-matching token")
    void findByCustomerWithAccess_WithNonMatchingToken_ShouldReturnEmptyPage() {
        // When: Finding items for basket1 with non-matching token
        Page<Item> itemPage = itemRepository.findByCustomerWithAccess(
                basket1.getId(), customer1.getId(), OWNER_TOKEN_2, NOT_ADMIN, Pageable.unpaged());

        // Then: Should return empty page (no access)
        assertThat(itemPage.getContent()).isEmpty();
        assertThat(itemPage.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findByCustomerWithAccess should respect pagination")
    void findByCustomerWithAccess_WithPagination_ShouldRespectPageSize() {
        // When: Finding items with page size 1
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<Item> itemPage = itemRepository.findByCustomerWithAccess(
                basket1.getId(), customer1.getId(), OWNER_TOKEN_1, NOT_ADMIN, pageRequest);

        // Then: Should return only one item but know about both
        assertThat(itemPage.getContent()).hasSize(1);
        assertThat(itemPage.getTotalElements()).isEqualTo(2);
        assertThat(itemPage.getTotalPages()).isEqualTo(2);
    }
}