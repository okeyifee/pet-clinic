package com.samuel.sniffers.api.exception;

public class CustomerAlreadyExistsException extends RuntimeException{

    @Override
    public String getMessage() {
        return "Customer with name already exist.";
    }
}
