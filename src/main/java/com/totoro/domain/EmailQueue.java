package com.totoro.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * This table stores a list of emails that need to be sent
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class EmailQueue extends BaseEntity implements Comparable<EmailQueue> {
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
    private String reason;

    public EmailQueue() {
    }

    public EmailQueue(String sender, String replyTo, String[] toRecipients, String[] ccRecipients, String[] bccRecipients,
                      String subject, String text, String type, String reason) {
        this.sender = sender;
        this.replyTo = replyTo;
        this.toRecipients = StringUtils.join(toRecipients, ";");
        this.ccRecipients = StringUtils.join(ccRecipients, ";");
        this.bccRecipients = StringUtils.join(bccRecipients, ";");
        this.subject = subject;
        this.text = text;
        this.type = type;
        this.reason = reason;
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
        return StringUtils.split(toRecipients);
    }

    public void setToRecipients(String[] recipients) {
        this.toRecipients = StringUtils.join(recipients, ";");
    }

    public String[] getCcRecipients() {
        return StringUtils.split(ccRecipients);
    }

    public void setCcRecipients(String[] ccRecipients) {
        this.ccRecipients = StringUtils.join(ccRecipients, ";");
    }

    public String[] getBccRecipients() {
        return StringUtils.split(bccRecipients);
    }

    public void setBccRecipients(String[] bccRecipients) {
        this.bccRecipients = StringUtils.join(bccRecipients, ";");
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int compareTo(@NotNull EmailQueue o) {
        return this.getCreatedAt().compareTo(o.getCreatedAt());
    }

}