package com.be.server.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;


public class EmailService {


    public static void sendEmail(String to, String subject, String content) {

        String host = "smtp.gmail.com";
        String from = "shopglamsole@gmail.com";
        // Replace with your newly generated App Password!
        String password = "ehsh dojk nqcr vapx";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            message.setContent(content, "text/html; charset=utf-8");

            // Gửi email
            Transport.send(message);
            System.out.println("Email sent successfully!"); // Add a success message

        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage()); // Print more specific error
            e.printStackTrace(); // Print stack trace for detailed debugging
            throw new RuntimeException("Failed to send email", e); // Re-throw with a more descriptive message
        }
    }
}


