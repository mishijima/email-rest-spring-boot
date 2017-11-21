package com.totoro.mails;

public abstract class MailRequest {
    final String from;
    final String[] to;
    final String[] cc;
    final String[] bcc;
    final String subject;
    final String text;
    final String type;

    MailRequest(String from, String[] to, String[] cc, String[] bcc, String subject, String text, String type) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.text = text;
        this.type = type;
    }

    public abstract String getData() throws Exception;

    public String getFrom() {
        return from;
    }

    public String[] getTo() {
        return to;
    }

    public String[] getCc() {
        return cc;
    }

    public String[] getBcc() {
        return bcc;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}
