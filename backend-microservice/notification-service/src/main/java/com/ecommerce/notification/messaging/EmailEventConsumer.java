package com.ecommerce.notification.messaging;

import com.ecommerce.notification.model.EmailRequest;
import com.ecommerce.notification.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class EmailEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    public EmailEventConsumer(EmailService emailService, ObjectMapper objectMapper) {
        this.emailService = emailService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${app.kafka.email-topic:email-notification}", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void consume(String payload) throws Exception {
        EmailRequest request = objectMapper.readValue(payload, EmailRequest.class);
        emailService.send(request);
    }
}
