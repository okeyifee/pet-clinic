package com.samuel.sniffers.controller.metrics;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/system-metrics")
public class PrometheusMetricsController {

    private final PrometheusMeterRegistry registry;

    public PrometheusMetricsController(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @GetMapping(produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> scrape() {
        return ResponseEntity
                .ok()
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(registry.scrape());
    }
}
