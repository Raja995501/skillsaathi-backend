package com.skillsaathi.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.skillsaathi.dto.auth.*;
import com.skillsaathi.entity.*;
import com.skillsaathi.exception.BadRequestException;
import com.skillsaathi.exception.ResourceNotFoundException;
import com.skillsaathi.exception.TokenRefreshException;
import com.skillsaathi.repository.*;
import com.skillsaathi.security.CustomUserDetails;
import com.skillsaathi.security.JwtService;
import com.skillsaathi.service.AuthService;
import com.skillsaathi.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private static final String DEFAULT_ROLE = "USER";

    @Override
    public void register(RegisterRequest request) {
        Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.isEmailVerified()) {
                throw new BadRequestException("An account with this email already exists");
            }

            existingUser.setName(request.getName());
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
            existingUser.setPhone(request.getPhone());
            existingUser.setCity(request.getCity());
            existingUser.setState(request.getState());
            userRepository.save(existingUser);

            emailVerificationTokenRepository.deleteByUser(existingUser);

            String token = UUID.randomUUID().toString();
            EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                    .token(token)
                    .user(existingUser)
                    .expiryDate(LocalDateTime.now().plusHours(24))
                    .build();
            emailVerificationTokenRepository.save(verificationToken);

            emailService.sendVerificationEmail(existingUser.getEmail(), existingUser.getName(), token);
            return;
        }

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Default role '" + DEFAULT_ROLE + "' not found. Seed roles before registering users."));

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .city(request.getCity())
                .state(request.getState())
                .role(userRole)
                .emailVerified(false)
                .blocked(false)
                .build();

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        emailVerificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);
    }

    @Override
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        emailVerificationTokenRepository.delete(verificationToken);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isBlocked()) {
            throw new BadRequestException("This account has been blocked. Contact support.");
        }
        if (!user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in.");
        }

        return buildAuthResponse(user);
    }

    // ✅ GOOGLE LOGIN IMPLEMENTATION
    @Override
    public AuthResponse googleLogin(GoogleTokenRequest request) {
        try {
            // 1. Google ID Token verify karein
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getToken());

            if (idToken == null) {
                throw new BadRequestException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // 2. User dhundhein ya naya banayein
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // Naya user banayein
                Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Default role '" + DEFAULT_ROLE + "' not found."));

                user = User.builder()
                        .name(name != null ? name : email.split("@")[0])
                        .email(email)
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .profilePictureUrl(picture)
                        .provider("GOOGLE")
                        .emailVerified(true)
                        .blocked(false)
                        .role(userRole)
                        .build();

                userRepository.save(user);
                log.info("New user created via Google: {}", email);
            } else {
                // Existing user — provider update karein
                if (user.getProvider() == null || "LOCAL".equals(user.getProvider())) {
                    user.setProvider("GOOGLE");
                    user.setEmailVerified(true);
                    if (user.getProfilePictureUrl() == null && picture != null) {
                        user.setProfilePictureUrl(picture);
                    }
                    userRepository.save(user);
                }

                if (user.isBlocked()) {
                    throw new BadRequestException("This account has been blocked. Contact support.");
                }
            }

            return buildAuthResponse(user);

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            throw new BadRequestException("Google authentication failed. Please try again.");
        }
    }

    @Override
    public AuthResponse refreshToken(String requestRefreshToken) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken, "Refresh token not found"));

        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new TokenRefreshException(requestRefreshToken, "Refresh token expired. Please log in again.");
        }

        User user = storedToken.getUser();
        refreshTokenRepository.delete(storedToken);

        return buildAuthResponse(user);
    }

    @Override
    public void logout(String requestRefreshToken) {
        refreshTokenRepository.findByToken(requestRefreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails, user.getId(), user.getRole().getName());

        String refreshTokenValue = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .build();
    }
}