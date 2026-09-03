package com.ecommerce.notification.service;

import com.ecommerce.notification.model.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${mail.host:smtp.gmail.com}")
    private String host;

    @Value("${mail.port:587}")
    private String port;

    @Value("${mail.username:}")
    private String username;

    @Value("${mail.password:}")
    private String password;

    public void send(EmailRequest request) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.warn("Email delivery suppressed because SMTP credentials are not configured; recipient={}, subject={}",
                    request.getTo(), request.getSubject());
            return;
        }

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTo()));
            message.setSubject(request.getSubject());
            message.setContent(request.getContent(), "text/html; charset=utf-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send email", e);
        }
    }
}
