package com.samuel.sniffers.repository;

import com.samuel.sniffers.entity.Customer;
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

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@ContextConfiguration(classes = CustomerRepositoryTest.TestConfig.class)
@Transactional
class CustomerRepositoryTest {

    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackages = "com.samuel.sniffers.entity")
    @EnableJpaRepositories(basePackages = "com.samuel.sniffers.repository")
    static class TestConfig {
        // Empty configuration class that enables Spring Boot auto-configuration
    }

    @Autowired
    private CustomerRepository customerRepository;

    private final String OWNER_TOKEN_1 = "token1";
    private final String OWNER_TOKEN_2 = "token2";
    private final boolean IS_ADMIN = true;
    private final boolean NOT_ADMIN = false;

    private Customer customer1;
    private Customer customer2;

    @BeforeEach
    void setUp() {
        // Clear any existing data
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
    }

    @Test
    @DisplayName("streamAllWithAccess should return all customers for admin")
    void streamAllWithAccess_AsAdmin_ShouldReturnAllCustomers() {
        // When: Streaming all customers as admin
        try (Stream<Customer> customerStream = customerRepository.streamAllWithAccess("any-token", IS_ADMIN)) {
            // Then: Should return all customers
            List<Customer> customers = customerStream.toList();
            assertThat(customers).hasSize(2);
            assertThat(customers).extracting(Customer::getId)
                    .contains(customer1.getId(), customer2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return only matching customers for non-admin")
    void streamAllWithAccess_AsNonAdmin_ShouldReturnOnlyMatchingCustomers() {
        // When: Streaming as token1 user
        try (Stream<Customer> customerStream = customerRepository.streamAllWithAccess(OWNER_TOKEN_1, NOT_ADMIN)) {
            // Then: Should return only customer1
            List<Customer> customers = customerStream.toList();
            assertThat(customers).hasSize(1);
            assertThat(customers.get(0).getId()).isEqualTo(customer1.getId());
        }

        // When: Streaming as token2 user
        try (Stream<Customer> customerStream = customerRepository.streamAllWithAccess(OWNER_TOKEN_2, NOT_ADMIN)) {
            // Then: Should return only customer2
            List<Customer> customers = customerStream.toList();
            assertThat(customers).hasSize(1);
            assertThat(customers.get(0).getId()).isEqualTo(customer2.getId());
        }
    }

    @Test
    @DisplayName("streamAllWithAccess should return empty stream for non-matching token")
    void streamAllWithAccess_WithNonMatchingToken_ShouldReturnEmptyStream() {
        // When: Streaming with invalid token
        try (Stream<Customer> customerStream = customerRepository.streamAllWithAccess("non-existing-token", NOT_ADMIN)) {
            // Then: Should return empty list
            List<Customer> customers = customerStream.toList();
            assertThat(customers).isEmpty();
        }
    }

    @Test
    @DisplayName("findAllWithAccess should return all customers for admin")
    void findAllWithAccess_AsAdmin_ShouldReturnAllCustomers() {
        // When: Finding all customers as admin
        List<Customer> customers = customerRepository.findAllWithAccess("any-token", IS_ADMIN);

        // Then: Should return all customers
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Customer::getId)
                .containsExactlyInAnyOrder(customer1.getId(), customer2.getId());
    }

    @Test
    @DisplayName("findAllWithAccess should return only matching customers for non-admin")
    void findAllWithAccess_AsNonAdmin_ShouldReturnOnlyMatchingCustomers() {
        // When: Finding as token1 user
        List<Customer> customers1 = customerRepository.findAllWithAccess(OWNER_TOKEN_1, NOT_ADMIN);

        // Then: Should return only customer1
        assertThat(customers1).hasSize(1);
        assertThat(customers1.get(0).getId()).isEqualTo(customer1.getId());

        // When: Finding as token2 user
        List<Customer> customers2 = customerRepository.findAllWithAccess(OWNER_TOKEN_2, NOT_ADMIN);

        // Then: Should return only customer2
        assertThat(customers2).hasSize(1);
        assertThat(customers2.get(0).getId()).isEqualTo(customer2.getId());
    }
}