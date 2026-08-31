package com.example.voting_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender,
                        @org.springframework.beans.factory.annotation.Value("${spring.mail.username:noreply@example.com}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendEmail(String to, String subject, String body) {
        if (mailSender == null) {
            // TEMPORARY DEMO OTP MODE: JavaMailSender is optional/unconfigured.
            System.out.println("Email Service: JavaMailSender unconfigured. Skipped sending email to: " + to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
            System.out.println("Email sent to " + to);
        } catch (Exception e) {
            System.err.println("FAILED TO SEND EMAIL: " + e.getMessage());
            System.out.println("\n\n=======================================================================");
            System.out.println("⚠️ EMAIL DELIVERY FAILED. YOUR OTP IS PRINTED BELOW:");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            System.out.println("=======================================================================\n\n");
        }
    }
}
