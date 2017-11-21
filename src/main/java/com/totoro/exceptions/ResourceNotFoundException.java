package com.totoro.exceptions;

import com.totoro.handlers.RestErrorHandler;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This class should be used when the caller requests a record that doesn't exist.
 *
 * @see RestErrorHandler#handleResourceNotFoundException
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -2493215172630829680L;

    public ResourceNotFoundException() {
        super();
    }

    /**
     * Uses the message and cause in Rest error handler
     *
     * @param message Message
     * @param cause   Cause
     * @see RestErrorHandler#handleResourceNotFoundException
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Uses the message in the Rest error handler
     *
     * @param message Message
     * @see RestErrorHandler#handleResourceNotFoundException
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Uses the cause in the Rest error handler
     *
     * @param cause Cause
     * @see RestErrorHandler#handleResourceNotFoundException
     */
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

}
