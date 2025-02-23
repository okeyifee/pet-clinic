package com.samuel.sniffers.api.exception;

import lombok.Getter;

@Getter
public class IllegalStateTransitionException extends RuntimeException {
    private final String currentState;
    private final String targetState;

    public IllegalStateTransitionException(String currentState, String targetState) {
        super(String.format("Invalid status transition from %s to %s", currentState, targetState));
        this.currentState = currentState;
        this.targetState = targetState;
    }
}
