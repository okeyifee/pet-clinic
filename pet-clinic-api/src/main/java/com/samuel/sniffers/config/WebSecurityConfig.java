package com.samuel.sniffers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
           .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for APIs (optional)
           .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()); // Allow all requests (you can customize access rules)

        return http.build();
    }
}
