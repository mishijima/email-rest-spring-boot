package com.totoro.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

public class ResponseFactory {
    public static <T> ResponseEntity createResponse(Collection<T> collection) {
        ListResponse<T> envelope = new ListResponse<>();
        envelope.data = collection;
        envelope.total = collection.size();

        return new ResponseEntity<ListResponse>(envelope, HttpStatus.OK);
    }

    public static <T> ResponseEntity createResponse(Collection<T> collection, HttpStatus httpStatus) {
        ListResponse<T> envelope = new ListResponse<>();
        envelope.data = collection;
        envelope.total = collection.size();

        return new ResponseEntity<ListResponse>(envelope, httpStatus);
    }

    public static <T> ResponseEntity createResponse(T item) {
        return new ResponseEntity<>(item, HttpStatus.OK);
    }

    public static <T> ResponseEntity createResponse(T item, HttpStatus httpStatus) {
        return new ResponseEntity<>(item, httpStatus);
    }

    public static ResponseEntity<Object> createError(String type, String[] messages, HttpStatus status) {
        ErrorResponse response = new ErrorResponse();
        response.type = type;
        response.messages = messages;
        response.code = status.value();

        return new ResponseEntity<Object>(response, status);
    }

    public static ResponseEntity<Object> createError(HttpStatus status, String[] messages) {
        return createError(status.getReasonPhrase(), messages, status);
    }

    public static ResponseEntity<Object> createError(String type, String message, HttpStatus status) {
        return createError(type, new String[]{message}, status);
    }

    public static ResponseEntity<Object> createError(HttpStatus status, String errorMessage) {
        return createError(status.getReasonPhrase(), errorMessage, status);
    }

    /**
     * The list response, it has the data and total
     *
     * @param <T> Type
     */
    private static class ListResponse<T> implements Response<T> {
        Collection<T> data;
        int total;
    }

    /**
     * The error response.
     */
    private static class ErrorResponse<T> implements Response<T> {
        @JsonProperty("error_type")
        String type;

        @JsonProperty("error_messages")
        String[] messages;

        int code;
    }
}
