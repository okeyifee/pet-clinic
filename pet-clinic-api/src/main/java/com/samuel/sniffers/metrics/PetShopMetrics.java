package com.samuel.sniffers.metrics;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.enums.BasketStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class PetShopMetrics {

    private final MeterRegistry meterRegistry;
    private final Logger logger;

    // Maps to store customer-specific AtomicIntegers for gauges
    private final Map<String, Map<String, AtomicInteger>> customerGauges = new ConcurrentHashMap<>();

    public PetShopMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    // Helper method to increment a counter with customer token tag
    private void incrementCounter(String metricName, String customerToken, int count) {
        int increment = count;
        while (increment > 0) {
            getOrCreateGauge(metricName, customerToken).incrementAndGet();
            increment--;
        }
    }

    // Helper method to get or create a customer-specific gauge
    private AtomicInteger getOrCreateGauge(String metricName, String customerToken) {
        Map<String, AtomicInteger> metricGauges = customerGauges.computeIfAbsent(metricName, k -> new ConcurrentHashMap<>());

        return metricGauges.computeIfAbsent(customerToken, token -> {
            AtomicInteger value = new AtomicInteger(0);
            Gauge.builder(metricName, value::get)
                    .tags(Tags.of("customerToken", token))
                    .register(meterRegistry);
            return value;
        });
    }

    // Counter increment methods
    public void setTotalCustomers(String customerToken, int count) {
        getOrCreateGauge("petshop.customers.total", customerToken).set(count);
    }

    public void incrementCustomerCreated(String customerToken) {
        logger.info("Updating metrics for customer created");
        incrementCounter("petshop.customers.created", customerToken, 1);
        getOrCreateGauge("petshop.customers.total", customerToken).incrementAndGet();
    }

    public void incrementCustomerDeleted(String customerToken) {
        logger.info("Updating metrics for customer deleted");
        logger.info("Updating metrics for deleted customer record");
        incrementCounter("petshop.customers.deleted", customerToken, 1);
    }

    public void incrementBasketCreated(String customerToken, int count) {
        logger.info("Updating metrics for shopping basket creation");
        incrementCounter("petshop.baskets.status:NEW", customerToken, count);
        getOrCreateGauge("petshop.baskets.active", customerToken).incrementAndGet();
    }

    public void incrementBasketPaid(String customerToken, int count) {
        logger.info("Updating metrics for shopping basket paid");
        incrementCounter("petshop.baskets.status:PAID", customerToken, count);
    }

    public void incrementBasketProcessed(String customerToken, int count) {
        logger.info("Updating metrics for shopping basket processed");
        incrementCounter("petshop.baskets.status:PROCESSED", customerToken, count);
    }

    public void incrementBasketUnknown(String customerToken, int count) {
        logger.info("Updating metrics for shopping basket with unknown status");
        incrementCounter("petshop.baskets.status:UNKWOWN", customerToken, count);
    }

    public void incrementBasketDeleted(String customerToken) {
        logger.info("Updating metrics for shopping basket deleted.");
        incrementCounter("petshop.baskets.deleted", customerToken, 1);
    }

    public void incrementItemAdded(String customerToken) {
        logger.info("Updating metrics for item created");
        incrementCounter("petshop.items.created", customerToken, 1);
    }

    public void incrementItemDeleted(String customerToken) {
        logger.info("Updating metrics for item deleted");
        incrementCounter("petshop.items.deleted", customerToken, 1);
    }

    public void incrementBasketStatus(String customerToken, BasketStatus basketStatus, int count) {
        logger.info("Updating basket transition metrics...");
        if (BasketStatus.NEW == basketStatus) {
            incrementBasketCreated(customerToken, count);
            return;
        }

        if (BasketStatus.PAID == basketStatus) {
            incrementBasketPaid(customerToken, count);
            updateBasketStatus("petshop.baskets.status:NEW", customerToken);
        }

        if (BasketStatus.PROCESSED == basketStatus) {
            incrementBasketProcessed(customerToken, count);
            updateBasketStatus("petshop.baskets.status:PAID", customerToken);
        }

        if (BasketStatus.UNKNOWN == basketStatus) {
            incrementBasketUnknown(customerToken, count);
        }

        incrementCounter("petshop.baskets.status_changes", customerToken, count);
    }

    // Update basket status with proper gauge tracking
    public void updateBasketStatus(String oldStatusName, String customerToken) {
        getOrCreateGauge(oldStatusName, customerToken).decrementAndGet();
    }
}
