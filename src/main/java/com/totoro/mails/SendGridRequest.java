package com.totoro.mails;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Send grid request builder to use this just call SendGridRequest.build(from, to[], subject, text) and chain it with cc(), bcc() and so on as needed
 * Refer to https://sendgrid.com/docs/API_Reference/Web_API_v3/index.html
 */
public class SendGridRequest extends MailRequest {

    SendGridRequest(Builder builder) {
        super(builder.from, builder.to, builder.cc, builder.bcc, builder.subject, builder.text, builder.type);
    }

    @Override
    public String getData() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        To[] toArr = null;
        if (to != null && to.length > 0) {
            toArr = new To[to.length];

            for (int i = 0; i < to.length; i++) {
                toArr[i] = new To(to[i]);
            }
        }

        Cc[] ccArr = null;
        if (cc != null && cc.length > 0) {
            ccArr = new Cc[cc.length];

            for (int i = 0; i < cc.length; i++) {
                ccArr[i] = new Cc(cc[i]);
            }
        }

        Bcc[] bccArr = null;
        if (bcc != null && bcc.length > 0) {
            bccArr = new Bcc[bcc.length];

            for (int i = 0; i < bcc.length; i++) {
                bccArr[i] = new Bcc(bcc[i]);
            }
        }

        Envelope envelope = new Envelope(
                new Personalization[]{
                        new Personalization(toArr, ccArr, bccArr, subject)
                },
                new From(from),
                new Content[]{
                        new Content(type, text)
                }
        );

        return objectMapper.writeValueAsString(envelope);
    }

    public static class Builder {
        private final String from;
        private final String[] to;
        private final String subject;
        private final String text;
        private String[] cc;
        private String[] bcc;
        private String type;

        public Builder(String from, String[] to, String subject, String text) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.text = text;
        }

        public Builder cc(String[] cc) {
            this.cc = cc;
            return this;
        }

        public Builder bcc(String[] bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public SendGridRequest build() {
            return new SendGridRequest(this);
        }
    }

    private static class Envelope {
        private Personalization[] personalizations;
        private From from;
        private Content[] content;

        Envelope(Personalization[] personalizations, From from, Content[] content) {
            this.personalizations = personalizations;
            this.from = from;
            this.content = content;
        }

        public Personalization[] getPersonalizations() {
            return personalizations;
        }

        public From getFrom() {
            return from;
        }

        public Content[] getContent() {
            return content;
        }
    }

    private static class Personalization {
        private To[] to;
        private Cc[] cc;
        private Bcc[] bcc;
        private String subject;

        Personalization(To[] to, Cc[] cc, Bcc[] bcc, String subject) {
            this.to = to;
            this.cc = cc;
            this.bcc = bcc;
            this.subject = subject;
        }

        public To[] getTo() {
            return to;
        }

        public String getSubject() {
            return subject;
        }

        public Cc[] getCc() {
            return cc;
        }

        public Bcc[] getBcc() {
            return bcc;
        }
    }

    private static class To {
        private String name;
        private String email;

        To(String email) {
            this.email = email;
        }

        To(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    private static class Cc {
        private String name;
        private String email;

        Cc(String email) {
            this.email = email;
        }

        Cc(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    private static class Bcc {
        private String name;
        private String email;

        Bcc(String email) {
            this.email = email;
        }

        Bcc(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    private static class From {
        private String email;

        From(String email) {
            this.email = email;
        }

        public String getEmail() {
            return email;
        }
    }

    private static class Content {
        private String type;
        private String value;

        Content(String type, String value) {
            this.type = type;
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

}
