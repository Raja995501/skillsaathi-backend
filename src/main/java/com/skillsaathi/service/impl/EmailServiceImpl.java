package com.skillsaathi.service.impl;

import com.skillsaathi.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via SMTP (JavaMailSender).
 * In local/dev, wiring a real SMTP server is optional — if it's not configured,
 * calls will simply fail silently-logged rather than crashing registration/reset flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String body = "Hi " + name + ",\n\nWelcome to Skill Equator! Verify your email:\n" + link
                + "\n\nThis link expires in 24 hours.";
        send(toEmail, "Verify your Skill Equator account", body);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String body = "Hi " + name + ",\n\nWe received a request to reset your password:\n" + link
                + "\n\nIf you didn't request this, ignore this email. This link expires in 1 hour.";
        send(toEmail, "Reset your SkillSaathi password", body);
    }

    private void send(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception ex) {
            // Don't let email delivery failures break the auth flow (e.g. SMTP not configured in dev).
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
