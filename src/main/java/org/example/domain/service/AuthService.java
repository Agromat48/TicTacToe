package org.example.domain.service;

import io.jsonwebtoken.Claims;
import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.domain.model.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final Map<UUID, String> mapTokens;

    public AuthService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        mapTokens = new ConcurrentHashMap<>();
    }

    public boolean registration(SignUpRequest signUpRequest) {
        if (signUpRequest == null
                || signUpRequest.getLogin() == null || signUpRequest.getLogin().isBlank()
                || signUpRequest.getPassword() == null || signUpRequest.getPassword().isBlank()) {
            return false;
        }

        String login = signUpRequest.getLogin();
        String password = passwordEncoder.encode(signUpRequest.getPassword());

        try {
            userService.createUser(login, password);
        }
        catch (UserAlreadyExistsException e) {
            return false;
        }

        return true;
    }

    public JwtResponse authorization(JwtRequest request) {
        if (request == null || request.getLogin() == null || request.getPassword() == null) {
            throw new InvalidCredentialsException("Invalid login or password");
        }

        User user = userService.findByLogin(request.getLogin())
                .filter(el -> passwordEncoder.matches(request.getPassword(), el.getPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid login or password"));

        JwtResponse response = generateTokens(user);
        mapTokens.put(user.getUuid(), response.getRefreshToken());

        return response;
    }

    public JwtResponse updateAccessToken(String refreshToken) {
        User user = getUserByRefreshToken(refreshToken);

        String newAccessToken = jwtProvider.generateAccessToken(user);

        return new JwtResponse(newAccessToken, null, "Bearer");
    }

    public JwtResponse updateRefreshToken(String refreshToken) {
        User user = getUserByRefreshToken(refreshToken);

        JwtResponse response = generateTokens(user);

        if (!mapTokens.replace(user.getUuid(), refreshToken, response.getRefreshToken())) {
            throw new InvalidCredentialsException("Refresh token has already been used");
        }

        return response;
    }

    private User getUserByRefreshToken(String refreshToken) {
        if (refreshToken == null || !jwtProvider.validateRefreshToken(refreshToken)) {
            throw new InvalidCredentialsException("Refresh token is invalid or expired");
        }

        Claims claims = jwtProvider.getRefreshClaims(refreshToken);

        UUID uuid = UUID.fromString(claims.get("uuid", String.class));

        if (!refreshToken.equals(mapTokens.get(uuid))) {
            throw new InvalidCredentialsException("Refresh token has already been used");
        }

        return userService.findByUuid(uuid)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
    }

    private JwtResponse generateTokens(User user) {
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        return new JwtResponse(accessToken, refreshToken, "Bearer");
    }

    public JwtAuthentication getJwtAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication instanceof JwtAuthentication jwtAuthentication) {
            return jwtAuthentication;
        }

        throw new InvalidCredentialsException("Don't get JwtAuthentication");
    }
}
