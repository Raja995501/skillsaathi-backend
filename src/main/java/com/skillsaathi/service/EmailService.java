package com.skillsaathi.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String name, String token);
    void sendPasswordResetEmail(String toEmail, String name, String token);
}
