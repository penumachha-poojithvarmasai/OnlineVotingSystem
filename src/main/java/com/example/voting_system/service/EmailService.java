package com.example.voting_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:noreply@example.com}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            if (mailSender != null) {
                mailSender.send(message);
                System.out.println("Email sent to " + to);
            } else {
                throw new RuntimeException("SMTP Mail Sender is disabled or unconfigured.");
            }
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
