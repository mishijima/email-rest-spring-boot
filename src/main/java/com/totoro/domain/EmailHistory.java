package com.totoro.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * This table stores a list of emails that have been sent
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EmailHistory extends BaseEntity implements Comparable<EmailHistory> {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private long id;
    private String sender;
    private String replyTo;
    private String toRecipients;
    private String ccRecipients;
    private String bccRecipients;
    private String subject;
    private String text;
    private String type;
    private String provider;
    /**
     * Unique id sender the provider
     */
    private String responseId;
    private String responseMessage;

    /**
     * Transient fields
     */
    @Transient
    private static final char DELIMITER = ';';

    public EmailHistory() {
    }

    public EmailHistory(String sender, String replyTo, String[] toRecipients, String[] ccRecipients, String[] bccRecipients, String subject, String text,
                        String type, String provider, String responseId, String responseMessage) {
        this.sender = sender;
        this.replyTo = replyTo;
        this.toRecipients = StringUtils.join(toRecipients, DELIMITER);
        this.ccRecipients = StringUtils.join(ccRecipients, DELIMITER);
        this.bccRecipients = StringUtils.join(bccRecipients, DELIMITER);
        this.subject = subject;
        this.text = text;
        this.type = type;
        this.provider = provider;
        this.responseId = responseId;
        this.responseMessage = responseMessage;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String[] getToRecipients() {
        return StringUtils.split(toRecipients, DELIMITER);
    }

    public void setToRecipients(String[] recipients) {
        this.toRecipients = StringUtils.join(recipients, DELIMITER);
    }

    public String[] getCcRecipients() {
        return StringUtils.split(ccRecipients, DELIMITER);
    }

    public void setCcRecipients(String[] ccRecipients) {
        this.ccRecipients = StringUtils.join(ccRecipients, DELIMITER);
    }

    public String[] getBccRecipients() {
        return StringUtils.split(bccRecipients, DELIMITER);
    }

    public void setBccRecipients(String[] bccRecipients) {
        this.bccRecipients = StringUtils.join(bccRecipients, DELIMITER);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public int compareTo(@NotNull EmailHistory o) {
        return this.getCreatedAt().compareTo(o.getCreatedAt());
    }
}
