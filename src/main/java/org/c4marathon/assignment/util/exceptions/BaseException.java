package org.c4marathon.assignment.util.exceptions;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final String errorCode;
    private final String message;
    private final String debugMessage;

    public BaseException(String errorCode, String message) {
        super(createMessage(errorCode, message, null));
        this.errorCode = errorCode;
        this.message = message;
        this.debugMessage = null;
    }

    public BaseException(String errorCode, String message, String debugMessage) {
        super(createMessage(errorCode, message, debugMessage));
        this.errorCode = errorCode;
        this.message = message;
        this.debugMessage = debugMessage;
    }

    private static String createMessage(String errorCode, String message, String debugMessage) {
        String newMessage = String.format("%s: %s", errorCode, message);

        if (newMessage != null && !newMessage.isEmpty()) {
            newMessage += ", " + newMessage;
        }

        return newMessage;
    }
}
