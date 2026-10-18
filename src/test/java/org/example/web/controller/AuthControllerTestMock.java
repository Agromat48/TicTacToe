package org.example.web.controller;

import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.model.JwtRequest;
import org.example.domain.model.JwtResponse;
import org.example.domain.model.SignUpRequest;
import org.example.domain.service.AuthService;
import org.example.web.model.request.RefreshJwtRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTestMock {

    @Mock
    private AuthService authService;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(authService);
    }

    @Test
    void registration_returnsOk_whenServiceReturnsTrue() {
        SignUpRequest request = new SignUpRequest("user", "12345");
        when(authService.registration(request)).thenReturn(true);

        ResponseEntity<Void> response = authController.registration(request);

        assertEquals(200, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(authService, times(1)).registration(request);
    }

    @Test
    void registration_returnsBadRequest_whenServiceReturnsFalse() {
        SignUpRequest request = new SignUpRequest("user", "12345");
        when(authService.registration(request)).thenReturn(false);

        ResponseEntity<Void> response = authController.registration(request);

        assertEquals(400, response.getStatusCode().value());
        assertNull(response.getBody());
        verify(authService, times(1)).registration(request);
    }

    @Test
    void authorization_returnsServiceResponse() {
        JwtRequest request = new JwtRequest("user", "12345");
        JwtResponse expected = new JwtResponse("access", "refresh", "Bearer");
        when(authService.authorization(request)).thenReturn(expected);

        JwtResponse response = authController.authorization(request);

        assertSame(expected, response);
        verify(authService).authorization(request);
    }

    @Test
    void authorization_throwsInvalidCredentials_whenServiceThrows() {
        JwtRequest request = new JwtRequest("user", "wrong");
        when(authService.authorization(request))
                .thenThrow(new InvalidCredentialsException("Invalid login or password"));

        assertThrows(InvalidCredentialsException.class, () -> authController.authorization(request));
    }

    @Test
    void updateAccessToken_passesRefreshTokenFromRequest() {
        JwtResponse expected = new JwtResponse("access", null, "Bearer");
        when(authService.updateAccessToken("refresh")).thenReturn(expected);

        JwtResponse response = authController.updateAccessToken(new RefreshJwtRequest("refresh"));

        assertSame(expected, response);
        verify(authService).updateAccessToken("refresh");
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenServiceThrows() {
        when(authService.updateAccessToken("refresh"))
                .thenThrow(new InvalidCredentialsException("Refresh token is invalid or expired"));

        assertThrows(InvalidCredentialsException.class,
                () -> authController.updateAccessToken(new RefreshJwtRequest("refresh")));
    }

    @Test
    void updateRefreshToken_passesRefreshTokenFromRequest() {
        JwtResponse expected = new JwtResponse("access", "new-refresh", "Bearer");
        when(authService.updateRefreshToken("refresh")).thenReturn(expected);

        JwtResponse response = authController.updateRefreshToken(new RefreshJwtRequest("refresh"));

        assertSame(expected, response);
        verify(authService).updateRefreshToken("refresh");
    }

    @Test
    void updateRefreshToken_throwsInvalidCredentials_whenServiceThrows() {
        when(authService.updateRefreshToken("refresh"))
                .thenThrow(new InvalidCredentialsException("Refresh token has already been used"));

        assertThrows(InvalidCredentialsException.class,
                () -> authController.updateRefreshToken(new RefreshJwtRequest("refresh")));
    }
}
