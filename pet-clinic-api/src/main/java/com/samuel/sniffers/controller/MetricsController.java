package com.samuel.sniffers.controller;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/metrics")
@io.swagger.v3.oas.annotations.tags.Tag(name = "metrics", description = "Retrieves Customer-specific app metrics.")
public class MetricsController {

    private final MetricService metricService;
    private final Logger logger;

    public MetricsController(MetricService metricService) {
        this.metricService = metricService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @GetMapping
    @Operation(
            summary = "Get metrics data",
            description = "Retrieves metrics data filtered by customer token"
    )
    public ResponseEntity<Map<String, Object>> getMetrics() {
        logger.info("Retrieving business metrics data.");
        return ResponseEntity.ok(metricService.getMetricsForCustomer());
    }
}