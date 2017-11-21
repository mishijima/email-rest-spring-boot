package com.totoro.dto;

/**
 * Email response that we return to the caller that triggers the send email api
 */
public class EmailResponseDto {

    private String message;
    private long timestamp;

    public EmailResponseDto() {
    }

    public EmailResponseDto(String message, long timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
