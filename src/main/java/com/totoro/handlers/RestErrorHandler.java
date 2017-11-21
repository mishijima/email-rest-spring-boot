package com.totoro.handlers;

import com.totoro.api.ResponseFactory;
import com.totoro.exceptions.BadRequestException;
import com.totoro.exceptions.ResourceNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * When our rest-api throws an error, one of the methods defined in this class will catch it, construct a human readable (or remove sensitive information) and finally send it
 * to the caller
 */
@ControllerAdvice("com.totoro.api")
public class RestErrorHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle an @Async exception because it cannot be caught withing the normal try and catch block
     *
     * @param ex      Exception
     * @param request Current request
     * @return Response to the caller
     */
    @ExceptionHandler(value = ExecutionException.class)
    public ResponseEntity<Object> handleExecutionException(Exception ex, WebRequest request) {
        logger.error("Error while processing request " + request.getDescription(true), ex);
        String errorMessage = "Internal exception";

        return new ResponseEntity<Object>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles resource not found exception
     *
     * @param ex      Exception
     * @param request Current request
     * @return Response to the caller
     */
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(Exception ex, WebRequest request) {
        String errorMessage;
        if (ex.getMessage() != null) {
            errorMessage = ex.getMessage();
        } else {
            errorMessage = "Resource not found";
        }

        return new ResponseEntity<Object>(errorMessage, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles bad request exception
     *
     * @param ex      Exception
     * @param request Current request
     * @return Response to the caller
     */
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
        if (ex.getMessages() != null && ex.getMessages().size() > 0) {
            String[] errorMessages = ex.getMessages().toArray(new String[ex.getMessages().size()]);
            return ResponseFactory.createError(HttpStatus.BAD_REQUEST, errorMessages);
        }

        return new ResponseEntity<Object>("Bad request", HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        String errorMessage;
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            logger.error("Error while processing request " + request.getDescription(true), ex);
            errorMessage = "Internal exception";
        } else {
            errorMessage = ex.getMessage();
        }

        return new ResponseEntity<Object>(errorMessage, status);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatus status, WebRequest request) {
        List<String> errorMessages = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessages.add("'" + fieldError.getField() + "' " + fieldError.getDefaultMessage());
        }

        return new ResponseEntity<Object>(errorMessages.toArray(new String[errorMessages.size()]), status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return new ResponseEntity<Object>("Bad request", HttpStatus.BAD_REQUEST);
    }
}
