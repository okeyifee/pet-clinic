package com.samuel.sniffers.security;

import com.samuel.sniffers.api.constants.SecurityConstants;
import com.samuel.sniffers.api.exception.UnauthorizedException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.config.TokenConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class SecurityService {

    private final TokenConfig tokenConfig;
    private final Logger logger;

    public SecurityService(TokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public String getCurrentCustomerToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String header = attributes.getRequest().getHeader(SecurityConstants.HEADER_AUTH);
            if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                return header.substring(SecurityConstants.TOKEN_PREFIX.length());
            }
        }
        logger.error("No valid token passed in request. Token is null.");
        throw new UnauthorizedException("No valid token found in request.");
    }

    public boolean isAdmin(String token) {
        return tokenConfig.getAdminToken().equals(token);
    }

    public boolean isCustomer(String token) {
        return tokenConfig.getCustomer1Token().equalsIgnoreCase(token) || tokenConfig.getCustomer2Token().equalsIgnoreCase(token);
    }

    public boolean isValidToken() {
        logger.debug("Validating if customer token is valid.");
        final String currentToken = getCurrentCustomerToken();
        return isAdmin(currentToken) || isCustomer(currentToken);
    }

    public void validateAccess(String resourceOwnerToken) {
        final String currentToken = getCurrentCustomerToken();
        if (!isAdmin(currentToken) && !currentToken.equals(resourceOwnerToken)) {
            throw new UnauthorizedException("Access denied.");
        }
    }
}
