package com.samuel.sniffers;

import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;

@SpringBootApplication(scanBasePackages = "com.samuel")
public class PetClinicApplication {

	public static void main(String[] args) {
		// Add unique correlation id at app startUp
		MDC.put("correlationId", LoggerFactory.getCorrelationId());

		LoggerFactory.getLogger(PetClinicApplication.class).info("Starting PetClinic application...");
		SpringApplication.run(PetClinicApplication.class, args);
	}
}
