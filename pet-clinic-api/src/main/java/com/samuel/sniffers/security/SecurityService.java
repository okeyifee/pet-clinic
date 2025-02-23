package com.samuel.sniffers.security;

import com.samuel.sniffers.api.constants.SecurityConstants;
import com.samuel.sniffers.api.exception.UnauthorizedException;
import com.samuel.sniffers.api.factory.LoggerFactory;
import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.config.TokenConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class SecurityService {

    private final TokenConfig tokenConfig;

    final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getCurrentCustomerToken() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        logger.info("Attributes: {}", attributes);
        if (attributes != null) {
            String header = attributes.getRequest().getHeader(SecurityConstants.HEADER_AUTH);
//            logger.info("Header: {}", header);
            if (header != null && header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
                return header.substring(SecurityConstants.TOKEN_PREFIX.length());
            }
        }
        throw new UnauthorizedException("No valid token found in request.");
    }

    public boolean isAdmin(String token) {
        return tokenConfig.getAdminToken().equals(token);
    }

    public boolean isCustomer(String token) {
        return tokenConfig.getCustomer1Token().equalsIgnoreCase(token) || tokenConfig.getCustomer2Token().equalsIgnoreCase(token);
    }

    public boolean isValidToken() {
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
