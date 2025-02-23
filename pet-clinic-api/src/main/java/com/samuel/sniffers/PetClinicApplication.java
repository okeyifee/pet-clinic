package com.samuel.sniffers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;

@SpringBootApplication(scanBasePackages = "com.samuel")
public class PetClinicApplication {

	public static void main(String[] args) {
		final Logger logger = LoggerFactory.getLogger(PetClinicApplication.class);

		logger.info("Starting PetClinic application...");
		SpringApplication.run(PetClinicApplication.class, args);
	}
}
