package com.totoro.services;

import com.totoro.domain.EmailHistory;
import com.totoro.domain.EmailQueue;
import com.totoro.dto.MailMessageDto;
import com.totoro.repository.EmailHistoryRepository;
import com.totoro.repository.EmailQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.Future;

/**
 * This class handles email async methods, we need to have it in a separate class since we cannot do self invocation of @Async method because it's running under the same thread
 * e.g. If you have an @Async method in the EmailService class then calling the method within the class itself won't make it Asynchronous.
 * <p>
 * It's also beneficial because we can call it from other services such as scheduler
 */
@Service
public class AsyncEmailService {

    private final EmailHistoryRepository emailRepository;
    private final EmailQueueRepository emailQueueRepository;

    @Autowired
    public AsyncEmailService(EmailHistoryRepository emailRepository, EmailQueueRepository emailQueueRepository) {
        this.emailRepository = emailRepository;
        this.emailQueueRepository = emailQueueRepository;
    }

    /**
     * This is an asynchronous method to save the sent email to the history table
     *
     * @param dto             Email message
     * @param responseId      Response id from the email provider
     * @param responseMessage Response message from the email provider
     * @param provider        Provider name. e.g SendGrid or MailGun
     * @return
     */
    @Async
    public Future<Long> saveToEmailHistory(MailMessageDto dto, String responseId, String responseMessage, String provider) {
        EmailHistory history = new EmailHistory(
                dto.getFrom(),
                dto.getReplyTo(),
                dto.getTo(),
                dto.getCc(),
                dto.getBcc(),
                dto.getSubject(),
                dto.getText(),
                dto.getType(),
                provider,
                responseId,
                responseMessage
        );

        history = emailRepository.save(history);

        return new AsyncResult<>(history.getId());
    }

    /**
     * This in an asynchronous method to save an email into a queue so it can be re-attempted.
     * Adding an email in this queue can be caused by many reasons, one is because the email providers are not available or it's because the client sends too many request at the same time.
     *
     * @param dto    Email message
     * @param reason Reason why we have it in the queue
     * @return
     */
    @Async
    public Future<Long> saveToEmailQueue(MailMessageDto dto, String reason) {
        EmailQueue queue = new EmailQueue(
                dto.getFrom(),
                dto.getReplyTo(),
                dto.getTo(),
                dto.getCc(),
                dto.getBcc(),
                dto.getSubject(),
                dto.getText(),
                dto.getType(),
                reason
        );

        queue = emailQueueRepository.save(queue);

        return new AsyncResult<>(queue.getId());
    }

}
