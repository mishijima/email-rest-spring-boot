package com.totoro.mails;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Mail gun request builder to use this just call MailGunRequest.build(from, to[], subject, text) and chain it with cc(), bcc() and so on as needed
 * Refer to https://documentation.mailgun.com/en/latest/user_manual.html#sending-via-api
 */
public class MailGunRequest extends MailRequest {

    MailGunRequest(Builder builder) {
        super(builder.from, builder.to, builder.cc, builder.bcc, builder.subject, builder.text, builder.type);
    }

    @Override
    public String getData() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append("from=").append(URLEncoder.encode(from, StandardCharsets.UTF_8.name()));

        if (to != null && to.length > 0) {
            for (String recipient : to) {
                sb.append("&to=").append(URLEncoder.encode(recipient, StandardCharsets.UTF_8.name()));
            }
        }

        if (cc != null && cc.length > 0) {
            for (String recipient : cc) {
                sb.append("&cc=").append(URLEncoder.encode(recipient, StandardCharsets.UTF_8.name()));
            }
        }

        if (bcc != null && bcc.length > 0) {
            for (String recipient : bcc) {
                sb.append("&bcc=").append(URLEncoder.encode(recipient, StandardCharsets.UTF_8.name()));
            }
        }

        sb.append("&subject=").append(URLEncoder.encode(subject, StandardCharsets.UTF_8.name()));
        sb.append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8.name()));

        return sb.toString();
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

        public MailGunRequest build() {
            return new MailGunRequest(this);
        }
    }

}
