package com.totoro.api;

import com.totoro.dto.MailMessageDto;
import com.totoro.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class EmailResource {

    private final EmailService emailService;

    @Autowired
    public EmailResource(EmailService emailService) {
        this.emailService = emailService;
    }

    @RequestMapping(value = "/api/emails", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity sendEmail(@Valid @RequestBody MailMessageDto mailMessage) throws Exception {
        return new ResponseEntity<>(emailService.sendEmail(mailMessage), HttpStatus.CREATED);
    }

}
