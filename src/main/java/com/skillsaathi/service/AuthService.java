package com.skillsaathi.service;

import com.skillsaathi.dto.auth.*;

public interface AuthService {
    void register(RegisterRequest request);
    void verifyEmail(String token);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String refreshToken);
    void logout(String refreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    AuthResponse googleLogin(GoogleTokenRequest request);
}
