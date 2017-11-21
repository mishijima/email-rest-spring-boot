package com.totoro.api;

import com.totoro.domain.EmailHistory;
import com.totoro.repository.EmailHistoryRepository;
import com.totoro.repository.EmailQueueRepository;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("Duplicates")
public class EmailResourceTest extends BaseResourceTest {

    @Autowired
    private EmailHistoryRepository emailHistoryRepository;
    @Autowired
    private EmailQueueRepository emailQueueRepository;

    @Before
    public void setup() throws Exception {
        this.emailHistoryRepository.deleteAllInBatch();
        this.emailQueueRepository.deleteAllInBatch();
    }

    /**
     * Test for sending email and the server should return 201
     *
     * @throws Exception If an exception occurs
     */
    @Test
    public void sendEmail_Success() throws Exception {
        long beforeSendTimestamp = new LocalDateTime().toDate().getTime();

        Map<String, Object> data = new HashMap<>();
        data.put("from", "noreply@example.org");
        data.put("to", new String[]{"john@example.org", "tom@example.org"});
        data.put("cc", new String[]{"keith@example.org", "steph@example.org"});
        data.put("bcc", new String[]{"james@example.org", "kevin@example.org"});
        data.put("subject", "This is a test subject");
        data.put("text", "This is a test body");

        RequestBuilder req = post("/api/emails")
                .contentType(APPLICATION_JSON_UTF8)
                .content(super.objectMapper.writeValueAsString(data));

        super.mockMvc.perform(req)
                .andExpect(status().isCreated())
                .andExpect(content().contentType(super.APPLICATION_JSON_UTF8))
                .andExpect(
                        jsonPath("$.message").value("Your email has been sent"))
                .andExpect(
                        jsonPath("$.timestamp", greaterThan(beforeSendTimestamp)));
    }

    /**
     * Test sending email, make sure that the server returns 201 and finally check if a record has been added to the history table
     *
     * @throws Exception If an exception occurs
     */
    @Test
    public void sendEmailAndCheckTheHistoryTable_Success() throws Exception {
        long beforeSendTimestamp = new LocalDateTime().toDate().getTime();

        Map<String, Object> data = new HashMap<>();
        data.put("from", "noreply@example.org");
        data.put("to", new String[]{"john@example.org", "tom@example.org"});
        data.put("cc", new String[]{"keith@example.org", "steph@example.org"});
        data.put("bcc", new String[]{"james@example.org", "kevin@example.org"});
        data.put("subject", "This is a test subject - " + beforeSendTimestamp);
        data.put("text", "This is a test body - " + beforeSendTimestamp);

        RequestBuilder req = post("/api/emails")
                .contentType(APPLICATION_JSON_UTF8)
                .content(super.objectMapper.writeValueAsString(data));

        super.mockMvc.perform(req)
                .andExpect(status().isCreated());

        Thread.sleep(100); // We need this because saving email into a table is on an @Async method
        EmailHistory history = emailHistoryRepository.findOne(1L);

        assertNotNull(history);
        assertEquals("noreply@example.org", history.getSender());
        assertEquals("john@example.org", history.getToRecipients()[0]);
        assertEquals("keith@example.org", history.getCcRecipients()[0]);
        assertEquals("james@example.org", history.getBccRecipients()[0]);
        assertEquals("This is a test subject - " + beforeSendTimestamp, history.getSubject());
        assertEquals("This is a test body - " + beforeSendTimestamp, history.getText());
    }

    /**
     * Test with bad email address where the server should return bad request and an error message
     *
     * @throws Exception If an exception occurs
     */
    @Test
    public void sendEmailWithBadEmail_BadRequestException() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("from", "noreply@example.org");
        data.put("to", new String[]{"john@example", "tom@example.org"});
        data.put("subject", "This is a test subject");
        data.put("text", "This is a test body");

        RequestBuilder req = post("/api/emails")
                .contentType(APPLICATION_JSON_UTF8)
                .content(super.objectMapper.writeValueAsString(data));

        super.mockMvc.perform(req)
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(super.APPLICATION_JSON_UTF8))
                .andExpect(
                        jsonPath("$.error_type").value("Bad Request"))
                .andExpect(
                        jsonPath("$.error_messages[0]").value("'to' email is invalid - john@example"));
    }

}
