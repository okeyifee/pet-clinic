package com.samuel.sniffers.security;

import com.samuel.sniffers.api.exception.UnauthorizedException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
//import java.util.Collections;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final SecurityService securityService;

    private final HandlerExceptionResolver handlerExceptionResolver;

    final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.contains("/swagger-ui") ||
                path.contains("/v3/api-docs") ||
                path.contains("/api/v3") ||
                path.contains("/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws UnauthorizedException, ServletException, IOException {

//        log.info("Request URI: {}", request.getRequestURI());
//        log.info("Request Headers: {}", Collections.list(request.getHeaderNames()));

        try {
            if (!securityService.isValidToken()) {
                throw new UnauthorizedException("Invalid token in request header.");
            }

            filterChain.doFilter(request, response);

        } catch (UnauthorizedException e) {
            handlerExceptionResolver.resolveException(request, response, null, e);
        }
    }
}
