package com.samuel.sniffers.api.factory;

import com.samuel.sniffers.api.logging.Logger;
import com.samuel.sniffers.internal.Slf4jLogger;

import java.util.UUID;

public class LoggerFactory {

    private LoggerFactory() {
        // Prevent instantiation of LoggerFactory
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static Logger getLogger(Class<?> clazz) {
        return new Slf4jLogger(clazz);
    }

    public static String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}