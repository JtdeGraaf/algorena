package com.algorena.common.exception;

/**
 * Exception thrown when communication with a bot endpoint fails.
 * This includes timeouts, connection errors, and invalid responses.
 */
public class BotCommunicationException extends RuntimeException {

    private final String reason;

    public BotCommunicationException(String message, String reason) {
        super(message);
        this.reason = reason;
    }

    public BotCommunicationException(String message, String reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
