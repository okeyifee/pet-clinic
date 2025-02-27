package com.samuel.sniffers.repository.view;

import com.samuel.sniffers.entity.Customer;
import com.samuel.sniffers.entity.Item;
import com.samuel.sniffers.entity.ShoppingBasket;
import com.samuel.sniffers.entity.view.CustomerBasketItemView;
import com.samuel.sniffers.enums.BasketStatus;
import com.samuel.sniffers.repository.CustomerRepository;
import com.samuel.sniffers.repository.ItemRepository;
import com.samuel.sniffers.repository.ShoppingBasketRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = CustomerBasketItemViewRepositoryTest.TestConfig.class)
@Transactional
class CustomerBasketItemViewRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.samuel.sniffers.entity")
    @EnableJpaRepositories(basePackages = "com.samuel.sniffers.repository")
    static class TestConfig {
        // Empty configuration class that enables Spring Boot auto-configuration
    }

    @Autowired
    private CustomerBasketItemViewRepository viewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ShoppingBasketRepository basketRepository;

    @Autowired
    private ItemRepository itemRepository;

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

    @PersistenceContext
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        // Clear existing data
        itemRepository.deleteAll();
        basketRepository.deleteAll();
        customerRepository.deleteAll();

        // Create and save test customers
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

        // Create and save test baskets
        basket1 = new ShoppingBasket();
        basket1.setCustomer(customer1);
        basket1.setStatus(BasketStatus.NEW);
        basket1.setItems(new HashSet<>());

        basket2 = new ShoppingBasket();
        basket2.setCustomer(customer2);
        basket2.setStatus(BasketStatus.PAID);
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
    void verifyViewCreation() {
        List<?> viewContents = entityManager.createNativeQuery("SELECT * FROM customer_basket_item_overview").getResultList();
        assertThat(viewContents).hasSize(3);
    }

    @Test
    @DisplayName("findAllWithAccess should return all view entries for admin")
    void findAllWithAccess_AsAdmin_ShouldReturnAllViewEntries() {
        // When: Finding all view entries as admin
        List<CustomerBasketItemView> viewEntries = viewRepository.findAllWithAccess("any-token", IS_ADMIN);

        // Then: Should return all view entries
        assertThat(viewEntries).hasSize(3);
        assertThat(viewEntries)
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsExactlyInAnyOrder(customer1.getId(), customer1.getId(), customer2.getId()); // Customer has 2 baskets
    }

    @Test
    @DisplayName("findAllWithAccess should return only matching view entries for non-admin")
    void findAllWithAccess_AsNonAdmin_ShouldReturnOnlyMatchingViewEntries() {
        // When: Finding view entries for token1
        List<CustomerBasketItemView> viewEntries1 = viewRepository.findAllWithAccess(OWNER_TOKEN_1, NOT_ADMIN);

        // Then: Should return only customer1's entries
        assertThat(viewEntries1)
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsExactlyInAnyOrder(customer1.getId(), customer1.getId());

        // When: Finding view entries for token2
        List<CustomerBasketItemView> viewEntries2 = viewRepository.findAllWithAccess(OWNER_TOKEN_2, NOT_ADMIN);

        // Then: Should return only customer2's entries
        assertThat(viewEntries2)
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsOnly(customer2.getId());
    }

    @Test
    @DisplayName("findAllWithAccess should return empty list for non-matching token")
    void findAllWithAccess_WithNonMatchingToken_ShouldReturnEmptyList() {
        // When: Finding view entries with invalid token
        List<CustomerBasketItemView> viewEntries = viewRepository.findAllWithAccess("non-existing-token", NOT_ADMIN);

        // Then: Should return empty list
        assertThat(viewEntries).isEmpty();
    }

    @Test
    @DisplayName("findAllWithAccess with pagination should return all view entries for admin")
    void findAllWithAccess_WithPagination_AsAdmin_ShouldReturnAllViewEntries() {
        // When: Finding all view entries as admin with pagination
        Page<CustomerBasketItemView> viewPage = viewRepository.findAllWithAccess("any-token", IS_ADMIN, Pageable.unpaged());

        // Then: Should return all view entries
        assertThat(viewPage.getContent()).isNotEmpty();
        assertThat(viewPage.getContent())
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsExactlyInAnyOrder(customer1.getId(), customer1.getId(), customer2.getId());
    }

    @Test
    @DisplayName("findAllWithAccess with pagination should return only matching view entries for non-admin")
    void findAllWithAccess_WithPagination_AsNonAdmin_ShouldReturnOnlyMatchingViewEntries() {
        // When: Finding view entries for token1 with pagination
        Page<CustomerBasketItemView> viewPage1 = viewRepository.findAllWithAccess(OWNER_TOKEN_1, NOT_ADMIN, Pageable.unpaged());

        // Then: Should return only customer1's entries
        assertThat(viewPage1.getContent())
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsExactlyInAnyOrder(customer1.getId(), customer1.getId());

        // When: Finding view entries for token2 with pagination
        Page<CustomerBasketItemView> viewPage2 = viewRepository.findAllWithAccess(OWNER_TOKEN_2, NOT_ADMIN, Pageable.unpaged());

        // Then: Should return only customer2's entries
        assertThat(viewPage2.getContent())
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsOnly(customer2.getId());
    }

    @Test
    @DisplayName("findAllWithAccess with pagination should respect page size")
    void findAllWithAccess_WithPagination_ShouldRespectPageSize() {
        // When: Finding view entries with page size 1
        PageRequest pageRequest = PageRequest.of(0, 1);
        Page<CustomerBasketItemView> viewPage = viewRepository.findAllWithAccess("any-token", IS_ADMIN, pageRequest);

        // Then: Should return only one entry but know about total entries
        assertThat(viewPage.getContent()).hasSize(1);
        assertThat(viewPage.getTotalElements()).isEqualTo(3);
        assertThat(viewPage.getTotalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("findByCustomerNameWithAccess should return view entries for specific customer name")
    void findByCustomerNameWithAccess_ShouldReturnViewEntriesForCustomerName() {
        // When: Finding view entries for specific customer name
        List<CustomerBasketItemView> viewEntries = viewRepository.findByCustomerNameWithAccess(
                "Test Customer 1", OWNER_TOKEN_1, NOT_ADMIN);

        // Then: Should return entries for the specified customer
        assertThat(viewEntries)
                .extracting(CustomerBasketItemView::getCustomerName)
                .containsExactlyInAnyOrder("Test Customer 1", "Test Customer 1");
        assertThat(viewEntries)
                .extracting(CustomerBasketItemView::getCustomerId)
                .containsExactlyInAnyOrder(customer1.getId(), customer1.getId());
    }
}
