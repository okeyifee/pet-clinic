package com.samuel.sniffers.api.exception;

public class EntityMappingException extends BaseException {
    public EntityMappingException(String message) {
        super(message, "MAPPING_ERROR", 500);
    }

    public EntityMappingException(String message, Throwable cause) {
        super(message, "MAPPING_ERROR", 500, cause);
    }
}