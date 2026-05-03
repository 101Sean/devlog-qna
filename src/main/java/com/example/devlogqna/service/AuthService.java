package com.example.devlogqna.service;

import com.example.devlogqna.dto.request.LoginRequest;
import com.example.devlogqna.dto.request.RefreshRequest;
import com.example.devlogqna.dto.response.TokenResponse;
import com.example.devlogqna.entity.User;
import com.example.devlogqna.repository.UserRepository;
import com.example.devlogqna.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public TokenResponse login(LoginRequest request) {
        // 1. 인증
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. 토큰 발급
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(username);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public TokenResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }
        String username = jwtTokenProvider.getUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String newAccessToken = jwtTokenProvider.generateAccessToken(username, user.getRole());
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // 기존 refresh 재사용
                .build();
    }

    public void logout(String accessToken) {
        String token = accessToken.substring(7); // "Bearer " 제거
        String username = jwtTokenProvider.getUsername(token);
        long remaining = jwtTokenProvider.getRemainingMillis(token);
        jwtTokenProvider.blacklistAccessToken(token, remaining);
        jwtTokenProvider.invalidateRefreshToken(username);
    }
}
