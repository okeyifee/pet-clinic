package com.samuel.sniffers.security;

import com.samuel.sniffers.api.exception.UnauthorizedException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityFilter extends OncePerRequestFilter {

    private final SecurityService securityService;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final Logger log;

    public SecurityFilter(SecurityService securityService, HandlerExceptionResolver handlerExceptionResolver) {
        this.securityService = securityService;
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.log = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/swagger-ui") ||
                path.contains("/v3/api-docs") ||
                path.contains("/api/v3") ||
                path.contains("/api-docs") ||
                path.contains("/actuator/health") ||
                path.contains("/api/v1/system-metrics") || // No auth for system-metrics endpoint
                path.contains("/actuator/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws UnauthorizedException, ServletException, IOException {

        // Add unique correlation id to any request
        MDC.put("correlationId", LoggerFactory.getCorrelationId());

        try {
            if (!securityService.isValidToken()) {
                log.error("Invalid token sent in request: {}", securityService.getCurrentCustomerToken());
                throw new UnauthorizedException("Invalid token in request header.");
            }

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
