package com.samuel.sniffers.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/*
  This class handles retrieving the configured tokens from properties file
  allowing for usage across several classes instead of having each class
  defining a SPELL expression on a variable.
*/
@Configuration
@ConfigurationProperties(prefix = "petshop.security")
@Getter
@Setter
public class TokenConfig {
    private String adminToken;
    private String customer1Token;
    private String customer2Token;
}
