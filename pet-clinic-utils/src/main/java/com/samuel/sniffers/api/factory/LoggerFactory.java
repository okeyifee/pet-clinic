package com.samuel.sniffers.api.factory;

import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.internal.Slf4jLogger;

public class LoggerFactory {

    private LoggerFactory() {
        // Private constructor to prevent instantiation
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Slf4jLogger(clazz);
    }
}