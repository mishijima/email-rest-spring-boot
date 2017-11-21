package com.totoro.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * This is the object that the client sends to us via our rest api
 */
public class MailMessageDto {
    private String from;

    private String replyTo;

    @Size(max = 10)
    private String[] to;

    @Size(max = 10)
    private String[] cc;

    @Size(max = 10)
    private String[] bcc;

    @NotNull
    private String subject;

    @NotNull
    private String text;

    private String type = "text/plain";

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public void setReplyTo(String replyTo) {
        this.replyTo = replyTo;
    }

    public String[] getTo() {
        if (to == null) {
            to = new String[0];
        }

        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public String[] getCc() {
        if (cc == null) {
            cc = new String[0];
        }

        return cc;
    }

    public void setCc(String[] cc) {
        this.cc = cc;
    }

    public String[] getBcc() {
        if (bcc == null) {
            bcc = new String[0];
        }

        return bcc;
    }

    public void setBcc(String[] bcc) {
        this.bcc = bcc;
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

}
