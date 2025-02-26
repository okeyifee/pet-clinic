package com.samuel.sniffers.metrics;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.security.SecurityService;
import com.samuel.sniffers.service.MetricService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricHelper implements MetricService {

    private final MeterRegistry meterRegistry;
    private final SecurityService securityService;
    private final Logger logger;

    public MetricHelper(MeterRegistry meterRegistry, SecurityService securityService) {
        this.meterRegistry = meterRegistry;
        this.securityService = securityService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public Map<String, Object> getMetricsForCustomer() {
        logger.info("Retrieving metrics information for customer");
        // Get the current token
        String customerToken = securityService.getCurrentCustomerToken();
        boolean isAdmin = securityService.isAdmin(customerToken);

        Map<String, Object> result = new HashMap<>();

        // For business metrics (petshop.*), we'll create category-based groupings
        Map<String, Map<String, Object>> businessMetrics = new HashMap<>();
        businessMetrics.put("customers", new HashMap<>());
        businessMetrics.put("baskets", new HashMap<>());
        businessMetrics.put("items", new HashMap<>());

        // keep system metrics in a separate group
        Map<String, Object> systemMetrics = new HashMap<>();

        // Process all metrics
        meterRegistry.getMeters().forEach(meter -> {
            String metricName = meter.getId().getName();
            List<Tag> tags = meter.getId().getTags();

            // Get the customer token from the tags if present
            String taggedToken = tags.stream()
                    .filter(tag -> "customerToken".equals(tag.getKey()))
                    .map(Tag::getValue)
                    .findFirst()
                    .orElse(null);

            // Only include the metric if it's for this logged in customer or user is admin
            if (metricName.startsWith("petshop.")) {
                if (taggedToken == null || isAdmin || customerToken.equals(taggedToken)) {
                    // Get the value - use the direct search option instead of measurements
                    Double value = extractValue(metricName, taggedToken);
                    if (value != null) {
                        // Add to the appropriate category
                        String shortName = metricName.substring("petshop.".length());
                        String category = shortName.split("\\.")[0]; // Get first segment after petshop.

                        Map<String, Object> categoryMap = businessMetrics.getOrDefault(category, new HashMap<>());

                        // For admin, include customer token in the key for proper separation
                        // This is to ensure admin can clearly differentiate whose token maps to each metric
                        String displayName = shortName;
                        if (isAdmin && taggedToken != null) {
                            displayName = taggedToken + "." + shortName;
                        }

                        categoryMap.put(displayName, value);
                        businessMetrics.put(category, categoryMap);
                    }
                }
            } else {
                // System metrics - include common ones for everyone
                if (shouldIncludeSystemMetric(metricName, isAdmin)) {
                    Double value = extractValue(metricName, null);
                    if (value != null) {
                        systemMetrics.put(metricName, value);
                    }
                }
            }
        });

        result.put("business", businessMetrics);
        result.put("system", systemMetrics);

        return result;
    }


    // Extract a numeric value from a meter by searching for it directly
    private Double extractValue(String metricName, String customerToken) {
        logger.info("Extracting value for metric with name {} from meter registry.", metricName);
        if (customerToken != null) {
            // Try to get customer-specific metric
            Gauge gauge = meterRegistry.find(metricName).tag("customerToken", customerToken).gauge();
            return gauge != null ? gauge.value() : null;
        } else {
            // Try to get a counter or gauge directly
            Gauge gauge = meterRegistry.find(metricName).gauge();
            if (gauge != null) {
                return gauge.value();
            }

            // If not found as gauge, try as counter
            return meterRegistry.find(metricName).counter() != null ?
                    meterRegistry.find(metricName).counter().count() : null;
        }
    }

    // Determine if a system metric should be included
    private boolean shouldIncludeSystemMetric(String metricName, boolean isAdmin) {
        logger.info("Checking customer role to determine data to be included in metrics.");
        // Admin can see all system metrics
        if (isAdmin) {
            return true;
        }

        // Only include non-sensitive metrics for regular users
        return metricName.startsWith("http.server.requests") ||
                metricName.startsWith("system.") ||
                metricName.startsWith("process.uptime") ||
                metricName.startsWith("jvm.memory.used") ||
                metricName.startsWith("jvm.threads.live");
    }
}
