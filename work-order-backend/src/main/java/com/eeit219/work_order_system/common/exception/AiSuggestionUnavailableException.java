package com.eeit219.work_order_system.common.exception;

public class AiSuggestionUnavailableException extends RuntimeException {

    public AiSuggestionUnavailableException(String message) {
        super(message);
    }

    public AiSuggestionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
