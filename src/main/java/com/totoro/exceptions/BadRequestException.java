package com.totoro.exceptions;

import com.totoro.handlers.RestErrorHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * This class should be used when the caller's request is invalid.
 *
 * @see RestErrorHandler#handleBadRequestException
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    private static final long serialVersionUID = 5712753410414501906L;

    private static final Logger logger = LoggerFactory.getLogger(BadRequestException.class);

    private List<String> messages = new ArrayList<>();

    public BadRequestException() {
        super();
    }

    /**
     * Uses the message and cause in Rest error handler
     *
     * @param message Message
     * @param cause   Cause
     * @see RestErrorHandler#handleBadRequestException
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Uses the message in Rest error handler
     *
     * @param message Message
     * @see RestErrorHandler#handleBadRequestException
     */
    public BadRequestException(String message) {
        super(message);
        messages.add(message);
    }

    public BadRequestException(List<String> messages) {
        super(StringUtils.join(messages, ","));
        this.messages.addAll(messages);
    }

    /**
     * Uses the message in Rest error handler
     *
     * @param cause Cause
     * @see RestErrorHandler#handleBadRequestException
     */
    public BadRequestException(Throwable cause) {
        super(cause);
    }

    /**
     * @return The list of messages
     */
    public List<String> getMessages() {
        return messages;
    }

}
