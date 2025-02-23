package com.samuel.sniffers.api.exception;

import lombok.Getter;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String code;
    private final int status;

    protected BaseException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    protected BaseException(String message, String code, int status, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.status = status;
    }
}