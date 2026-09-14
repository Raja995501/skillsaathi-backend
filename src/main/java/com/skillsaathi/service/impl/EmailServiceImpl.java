package com.skillsaathi.service.impl;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.skillsaathi.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Sends transactional emails via Resend's HTTP API.
 * Using HTTP (port 443) instead of raw SMTP (port 587) because many hosting
 * platforms (e.g. Render free tier) block outbound SMTP ports.
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.resend.api-key}")
    private String resendApiKey;

    @Value("${app.resend.from-email:noreply@skillequator.in}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;
        String html = "<p>Hi " + name + ",</p>"
                + "<p>Welcome to Skill Equator! Please verify your email by clicking the link below:</p>"
                + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                + "<p>This link expires in 24 hours.</p>";
        send(toEmail, "Verify your Skill Equator account", html);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String name, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;
        String html = "<p>Hi " + name + ",</p>"
                + "<p>We received a request to reset your password. Click the link below to reset it:</p>"
                + "<p><a href=\"" + link + "\">" + link + "</a></p>"
                + "<p>If you didn't request this, ignore this email. This link expires in 1 hour.</p>";
        send(toEmail, "Reset your Skill Equator password", html);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(fromEmail)
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(params);
            log.info("Email sent successfully to {}", to);
        } catch (Exception ex) {
            // Don't let email delivery failures break the auth flow
            // (e.g. Resend's testing-mode restriction: can only send to the
            // account owner's email until a custom domain is verified).
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
        }
    }
}
