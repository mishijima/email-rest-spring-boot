package com.totoro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({
        @PropertySource("classpath:mailgun-mail.properties"),
        @PropertySource("classpath:mailgun-mail-test.properties")
})
public class MailGunHttpConfiguration {

    @Value("${second.mail.http-api.provider}")
    private String provider;
    @Value("${second.mail.http-api.from}")
    private String from;
    @Value("${second.mail.http-api.url}")
    private String url;
    @Value("${second.mail.http-api.request-method}")
    private String requestMethod;
    @Value("${second.mail.http-api.key}")
    private String key;
    @Value("${second.mail.http-api.content-type}")
    private String contentType;
    @Value("${second.mail.http-api.accept-type}")
    private String acceptType;

    private String redirectUrl;

    public String getProvider() {
        return provider;
    }

    public String getFrom() {
        return from;
    }

    public String getUrl() {
        return url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getKey() {
        return key;
    }

    public String getContentType() {
        return contentType;
    }

    public String getAcceptType() {
        return acceptType;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
