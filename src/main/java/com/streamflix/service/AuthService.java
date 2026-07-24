package com.streamflix.service;

import com.streamflix.dto.request.GoogleAuthRequest;
import com.streamflix.dto.request.LoginRequest;
import com.streamflix.dto.request.RefreshTokenRequest;
import com.streamflix.dto.request.RegisterRequest;
import com.streamflix.dto.response.AuthResponse;
import com.streamflix.entity.Role;
import com.streamflix.entity.User;
import com.streamflix.exception.DuplicateResourceException;
import com.streamflix.exception.InvalidCredentialsException;
import com.streamflix.repository.UserRepository;
import com.streamflix.security.GoogleTokenVerifier;
import com.streamflix.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .enabled(true)
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        String email = jwtService.extractUsername(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));

        if (!jwtService.isRefreshToken(token) || !jwtService.isTokenValid(token, user)) {
            throw new InvalidCredentialsException("Invalid or expired refresh token");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        GoogleTokenVerifier.GoogleUser googleUser = googleTokenVerifier.verify(request.getIdToken());

        if (!googleUser.emailVerified()) {
            throw new InvalidCredentialsException("Google reports this email address isn't verified.");
        }

        String email = googleUser.email().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .fullName(googleUser.name())
                    .email(email)
                    .password(passwordEncoder.encode(randomPassword())) // never used to log in; satisfies the not-null column
                    .role(Role.ROLE_USER)
                    .enabled(true)
                    .build();
            userRepository.save(newUser);
            return newUser;
        });

        return buildAuthResponse(user);
    }

    private String randomPassword() {
        byte[] bytes = new byte[24];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
