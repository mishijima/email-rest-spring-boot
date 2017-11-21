package com.totoro.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.totoro.config.MailGunHttpConfiguration;
import com.totoro.config.SendGridHttpConfiguration;
import com.totoro.dto.EmailResponseDto;
import com.totoro.dto.MailMessageDto;
import com.totoro.exceptions.BadRequestException;
import com.totoro.mails.MailGunRequest;
import com.totoro.mails.MailGunResponse;
import com.totoro.mails.MailRequest;
import com.totoro.mails.SendGridErrorResponse;
import com.totoro.mails.SendGridRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private static final int CONNECT_TIMEOUT = 3000;
    private final SendGridHttpConfiguration sendGridHttpConfig;
    private final MailGunHttpConfiguration mailGunHttpConfig;
    private final AsyncEmailService emailAsyncService;
    /**
     * This will be set to 'true' when the first email fails
     */
    private boolean useSecondMail = false;

    @Autowired
    public EmailServiceImpl(SendGridHttpConfiguration sendGridHttpConfig, MailGunHttpConfiguration mailGunHttpConfig,
                            AsyncEmailService emailAsyncService) {
        this.sendGridHttpConfig = sendGridHttpConfig;
        this.mailGunHttpConfig = mailGunHttpConfig;
        this.emailAsyncService = emailAsyncService;
    }

    @Override
    public EmailResponseDto sendEmail(MailMessageDto dto) throws Exception {
        // TODO throttle the request
        // Validate the mail message
        List<String> errors = validate(dto);
        // If we found at least an error just cancel the request straight away
        if (errors.size() > 0) {
            throw new BadRequestException(errors);
        }

        // Health check
        if (healthCheck(sendGridHttpConfig.getUrl())) {
            useSecondMail = false;
        } else if (healthCheck(mailGunHttpConfig.getUrl())) {
            useSecondMail = true;
        } else {
            String reason = "Both providers couldn't be reached!!!";
            logger.warn(reason);

            // Save it into the queue table so we can go back to it and re-attempt
            emailAsyncService.saveToEmailQueue(dto, reason);

            // TODO notify the person responsible
            // Tell the user that their email has been put into the queue
            return new EmailResponseDto("Your email has been added into the queue", new Date().getTime());
        }

        HttpURLConnection conn = connectAndSendData(dto);

        int responseCode = conn.getResponseCode();
        // The following redirect handling is very simple - I don't expect it to happen because normally redirect happens from http to https and also I don't expect the provider to change the url endpoint
        // Handle 301 or 302 - If the url has been marked as 301 or 302 then let the personal responsible know
        if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            // TODO: Send a notification to the person responsible
            String message = "The request gets redirected, this is not supported yet so please check and update the url accordingly in the config - redirected url: " + conn.getHeaderField("Location");
            logger.warn(message);

            // Let's do it again with the new url
            String redirectUrl = conn.getHeaderField("Location");
            if (!useSecondMail) {
                sendGridHttpConfig.setRedirectUrl(redirectUrl);
            } else {
                mailGunHttpConfig.setRedirectUrl(redirectUrl);
            }
            conn = connectAndSendData(dto);

            responseCode = conn.getResponseCode();
        }

        // Handle normal and error stream
        boolean errorStream = false;
        String responseMsg = "Your email has been sent";
        InputStream is;
        if (responseCode < HttpURLConnection.HTTP_BAD_REQUEST) {
            is = conn.getInputStream();
        } else {
            errorStream = true;
            is = conn.getErrorStream();
        }

        // Parse the input stream
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        if (errorStream) {
            // Save it into the queue table so we can go back to it and re-attempt
            emailAsyncService.saveToEmailQueue(dto, response.toString());

            // TODO notify the person responsible
            // Tell the user that their email has been put into the queue
            return new EmailResponseDto("Your email has been added into the queue", new Date().getTime());
        }

        // Use future to do this as we don't want the user to wait for this process to finish
        prepareAndSaveToEmailHistory(dto, response, conn, useSecondMail);

        return new EmailResponseDto(responseMsg, new Date().getTime());
    }

    /**
     * Prepares the data to be saved to the email history table
     *
     * @param dto           Mail message from the client
     * @param response      Response from the mail provider
     * @param conn          Http connection
     * @param useSecondMail True if we are using the main provider
     * @throws IOException If an exception occurs
     */
    private void prepareAndSaveToEmailHistory(MailMessageDto dto, StringBuilder response, HttpURLConnection conn, boolean useSecondMail) throws Exception {
        String provider;
        String responseId;
        String responseMessage;
        ObjectMapper objectMapper = new ObjectMapper();

        if (!useSecondMail) {
            provider = sendGridHttpConfig.getProvider();
            responseId = conn.getHeaderField("X-Message-ID");
            responseMessage = response.toString();
            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                SendGridErrorResponse errorResponse = objectMapper.readValue(response.toString(), SendGridErrorResponse.class);
                if (errorResponse.getErrors() != null && errorResponse.getErrors().length > 0) {
                    responseMessage = errorResponse.getErrors()[0].getMessage();
                }
                logger.error(objectMapper.writeValueAsString(errorResponse));
            }
        } else {
            MailGunResponse mailGunResponse = objectMapper.readValue(response.toString(), MailGunResponse.class);
            provider = mailGunHttpConfig.getProvider();
            responseId = mailGunResponse.getId();
            responseMessage = mailGunResponse.getMessage();
        }

        emailAsyncService.saveToEmailHistory(dto, responseId, responseMessage, provider);
    }

    /**
     * Establishes a http connection and then send the data
     *
     * @param dto Mail message from the client
     * @return Http connection
     * @throws Exception If an exception occurs
     */
    private HttpURLConnection connectAndSendData(MailMessageDto dto) throws Exception {
        // Construct the data
        byte[] data = buildMailData(dto);
        // Build the connection
        HttpURLConnection conn = buildConn(String.valueOf(data.length));
        // Let's write the request data
        writeToOutputStream(conn, data);

        return conn;
    }

    /**
     * A helper method to write the data to the output stream
     *
     * @param conn Http connection
     * @param data Data
     * @throws IOException If an exception occurs
     */
    private void writeToOutputStream(HttpURLConnection conn, byte[] data) throws IOException {
        OutputStream os = conn.getOutputStream();
        os.write(data);
        os.close();
    }

    /**
     * This method goes through 'to', 'cc' and 'bcc' arrays and make sure there are no duplicates in the list
     *
     * @param errors List of errors
     * @param dto    Mail message from the client
     */
    private void checkDuplicateRecipients(List<String> errors, MailMessageDto dto) {
        Set<String> toSet = new HashSet<>();
        Set<String> ccSet = new HashSet<>();
        Set<String> bccSet = new HashSet<>();

        Set<String> duplicates = new HashSet<>();
        for (String to : dto.getTo()) {
            if (toSet.contains(to)) {
                duplicates.add(to);
            }
            toSet.add(to);
        }

        for (String cc : dto.getCc()) {
            if (toSet.contains(cc) || ccSet.contains(cc)) {
                duplicates.add(cc);
            }
            ccSet.add(cc);
        }

        for (String bcc : dto.getBcc()) {
            if (toSet.contains(bcc) || ccSet.contains(bcc) || bccSet.contains(bcc)) {
                duplicates.add(bcc);
            }
            bccSet.add(bcc);
        }
        if (duplicates.size() > 0) {
            errors.add(String.format("Email address in to, cc and bcc should be unique - %s", StringUtils.join(duplicates, ",")));
        }
    }

    /**
     * This method goes through a few validations to make sure that some of the mandatory fields exist, emails are in good format and no duplicates.
     * We could make these as constraints and use them on the dto. E.g @Email, @Duplicates, etc
     *
     * @param dto Mail message from the client
     * @return List of errors
     */
    private List<String> validate(MailMessageDto dto) {
        List<String> errors = new ArrayList<>();

        // Mandatory check - the from and to email need to exist
        if (dto.getFrom() == null || "".equals(dto.getFrom())) {
            errors.add("From email is missing");

            return errors;
        } else if ((dto.getTo().length == 0) && (dto.getCc().length == 0) && (dto.getBcc().length == 0)) {
            errors.add("No no, cannot send an email without any recipients");

            return errors;
        }

        // Email address format check
        EmailValidator emailValidator = EmailValidator.getInstance();
        checkEmailFormat(errors, emailValidator, new String[]{
                dto.getFrom()
        }, "from");

        checkEmailFormat(errors, emailValidator, dto.getTo(), "to");
        checkEmailFormat(errors, emailValidator, dto.getCc(), "cc");
        checkEmailFormat(errors, emailValidator, dto.getBcc(), "bcc");

        // Check duplicate recipients because some providers will reject duplicates
        checkDuplicateRecipients(errors, dto);

        return errors;
    }

    /**
     * A helper method to validate if the email address is in a good format
     *
     * @param errors    List of errors
     * @param validator Email validator
     * @param emails    An array of emails
     * @param type      To | Cc | Bcc
     */
    private void checkEmailFormat(List<String> errors, EmailValidator validator, String[] emails, String type) {
        for (String email : emails) {
            if (!validator.isValid(email)) {
                errors.add(String.format("'%s' email is invalid - %s", type, email));
            }
        }
    }

    /**
     * Constructs the request body based on the 'useSecondMail' flag
     *
     * @param dto Mail message from the client
     * @return Data in byte array
     * @throws Exception If an exception occurs
     */
    private byte[] buildMailData(MailMessageDto dto) throws Exception {
        byte[] data;

        MailRequest request;
        if (!useSecondMail) {
            request = new SendGridRequest.Builder(dto.getFrom(), dto.getTo(), dto.getSubject(), dto.getText())
                    .cc(dto.getCc())
                    .bcc(dto.getBcc())
                    .type(dto.getType())
                    .build();
        } else {
            request = new MailGunRequest.Builder(dto.getFrom(), dto.getTo(), dto.getSubject(), dto.getText())
                    .cc(dto.getCc())
                    .bcc(dto.getBcc())
                    .type(dto.getType())
                    .build();
        }
        data = request.getData().getBytes();

        return data;
    }

    /**
     * Builds a Http connection by using SendGrid when 'useSecondMail' is false and using MailGun when 'useSecondMail' is true
     *
     * @param dataLength Length of the content
     * @return Http connection for SendGrid or MailGun
     * @throws Exception When a connection to the server cannot be established
     */
    private HttpURLConnection buildConn(String dataLength) throws IOException {
        HttpURLConnection conn;
        try {
            if (!useSecondMail) {
                conn = buildSendGridConn(dataLength);
            } else {
                conn = buildMailGunConn(dataLength);
            }
        } catch (IOException e) {
            logger.error("Could not connect to the mail provider");
            throw new IOException("Could not connect to the mail provider");
        }

        return conn;
    }

    /**
     * This method creates a Http connection for SendGrid
     *
     * @param dataLength Length of the content
     * @return Http connection for SendGrid
     * @throws IOException When a connection to the server cannot be established
     */
    private HttpURLConnection buildSendGridConn(String dataLength) throws IOException {
        HttpURLConnection conn = null;

        try {
            String urlStr = sendGridHttpConfig.getRedirectUrl() == null ? sendGridHttpConfig.getUrl() : sendGridHttpConfig.getRedirectUrl();
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod(sendGridHttpConfig.getRequestMethod());

            conn.setRequestProperty("Content-Type", sendGridHttpConfig.getContentType());
            conn.setRequestProperty("Content-Length", dataLength);
            conn.setRequestProperty("Accept", sendGridHttpConfig.getAcceptType());
            conn.setRequestProperty("Authorization", "Bearer " + sendGridHttpConfig.getKey());

            conn.setDoOutput(true);
            conn.setDoInput(true);
        } catch (IOException e) {
            logger.error("Could not connect to the first mail provider");
        }

        return conn;
    }

    /**
     * This method creates a Http connection for MailGun
     *
     * @param dataLength Length of the content
     * @return Http connection for MailGun
     * @throws IOException When a connection to the server cannot be established
     */
    private HttpURLConnection buildMailGunConn(String dataLength) throws IOException {
        HttpURLConnection conn = null;

        try {
            String urlStr = mailGunHttpConfig.getRedirectUrl() == null ? mailGunHttpConfig.getUrl() : mailGunHttpConfig.getRedirectUrl();
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();

            // Set the user and password
            Authenticator.setDefault(new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("api", mailGunHttpConfig.getKey().toCharArray());
                }
            });

            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setInstanceFollowRedirects(false);

            conn.setRequestMethod(mailGunHttpConfig.getRequestMethod());
            conn.setRequestProperty("Content-Type", mailGunHttpConfig.getContentType());
            conn.setRequestProperty("Content-Length", dataLength);

            conn.setDoOutput(true);
            conn.setDoInput(true);
        } catch (IOException e) {
            logger.error("Could not connect to the second mail provider");
        }

        return conn;
    }

    /**
     * A simple way of checking if the server is responding
     *
     * @param targetUrl The server url
     * @return True if the server returns HTTP_OK or False if the server cannot be reached
     * @throws Exception When the server cannot be reached
     */
    private boolean healthCheck(String targetUrl) throws Exception {
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("OPTIONS");

            return conn.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            // TODO: Send a notification to the person responsible
            logger.error("Ummm, it looks like I couldn't establish a connection to " + e.getMessage());
            return false;
        }
    }

}
