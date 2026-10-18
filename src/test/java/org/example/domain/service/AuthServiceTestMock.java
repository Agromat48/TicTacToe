package org.example.domain.service;

import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.JwtRequest;
import org.example.domain.model.JwtResponse;
import org.example.domain.model.Role;
import org.example.domain.model.SignUpRequest;
import org.example.domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTestMock {
    private static final String ACCESS_SECRET = "test-access-secret-key-must-be-at-least-32-bytes";
    private static final String REFRESH_SECRET = "test-refresh-secret-key-must-be-at-least-32-bytes";

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtProvider jwtProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(ACCESS_SECRET, REFRESH_SECRET, 15, 30);
        authService = new AuthService(userService, passwordEncoder, jwtProvider);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registration_returnsTrue_andSavesHashedPassword() {
        when(passwordEncoder.encode("12345")).thenReturn("hash");

        boolean result = authService.registration(new SignUpRequest("user", "12345"));

        assertTrue(result);
        verify(userService).createUser("user", "hash");
    }

    @Test
    void registration_returnsFalse_whenUserAlreadyExists() {
        when(passwordEncoder.encode("12345")).thenReturn("hash");
        when(userService.createUser("user", "hash"))
                .thenThrow(new UserAlreadyExistsException("User with login 'user' already exists"));

        boolean result = authService.registration(new SignUpRequest("user", "12345"));

        assertFalse(result);
    }

    @Test
    void registration_returnsFalse_whenRequestIsNull() {
        assertFalse(authService.registration(null));

        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void registration_returnsFalse_whenLoginIsBlank() {
        assertFalse(authService.registration(new SignUpRequest("   ", "12345")));

        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void registration_returnsFalse_whenPasswordIsNull() {
        assertFalse(authService.registration(new SignUpRequest("user", null)));

        verifyNoInteractions(userService, passwordEncoder);
    }

    @Test
    void authorization_throwsInvalidCredentials_whenRequestIsNull() {
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.authorization(null));

        assertEquals("Invalid login or password", exception.getMessage());
        verifyNoInteractions(userService);
    }

    @Test
    void authorization_throwsInvalidCredentials_whenLoginIsNull() {
        assertThrows(InvalidCredentialsException.class,
                () -> authService.authorization(new JwtRequest(null, "12345")));

        verifyNoInteractions(userService);
    }

    @Test
    void authorization_returnsTokens_whenCredentialsAreValid() {
        User user = createUser();
        when(userService.findByLogin("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("12345", "hash")).thenReturn(true);

        JwtResponse response = authService.authorization(new JwtRequest("user", "12345"));

        assertAll(
                () -> assertEquals("Bearer", response.getType()),
                () -> assertTrue(jwtProvider.validateAccessToken(response.getAccessToken())),
                () -> assertTrue(jwtProvider.validateRefreshToken(response.getRefreshToken())),
                () -> assertEquals(user.getUuid().toString(),
                        jwtProvider.getAccessClaims(response.getAccessToken()).get("uuid", String.class))
        );
    }

    @Test
    void authorization_throwsInvalidCredentials_whenUserNotFound() {
        when(userService.findByLogin("user")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.authorization(new JwtRequest("user", "12345")));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void authorization_throwsInvalidCredentials_whenPasswordDoesNotMatch() {
        when(userService.findByLogin("user")).thenReturn(Optional.of(createUser()));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.authorization(new JwtRequest("user", "wrong")));
    }

    @Test
    void updateAccessToken_returnsNewAccessToken_whenRefreshTokenIsActual() {
        User user = createUser();
        JwtResponse loginResponse = login(user);
        when(userService.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        JwtResponse response = authService.updateAccessToken(loginResponse.getRefreshToken());

        assertAll(
                () -> assertEquals("Bearer", response.getType()),
                () -> assertTrue(jwtProvider.validateAccessToken(response.getAccessToken())),
                () -> assertNull(response.getRefreshToken())
        );
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenTokenIsNull() {
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateAccessToken(null));

        assertEquals("Refresh token is invalid or expired", exception.getMessage());
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenTokenIsGarbage() {
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateAccessToken("not a token"));

        assertEquals("Refresh token is invalid or expired", exception.getMessage());
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenAccessTokenPassed() {
        String accessToken = login(createUser()).getAccessToken();

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateAccessToken(accessToken));

        assertEquals("Refresh token is invalid or expired", exception.getMessage());
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenTokenWasNotIssuedOnLogin() {
        String refreshToken = jwtProvider.generateRefreshToken(createUser());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateAccessToken(refreshToken));

        assertEquals("Refresh token has already been used", exception.getMessage());
        verify(userService, never()).findByUuid(any());
    }

    @Test
    void updateAccessToken_throwsInvalidCredentials_whenUserWasDeleted() {
        User user = createUser();
        JwtResponse loginResponse = login(user);
        when(userService.findByUuid(user.getUuid())).thenReturn(Optional.empty());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateAccessToken(loginResponse.getRefreshToken()));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void updateRefreshToken_returnsNewPair_whenRefreshTokenIsActual() {
        User user = createUser();
        JwtResponse loginResponse = login(user);
        when(userService.findByUuid(user.getUuid())).thenReturn(Optional.of(user));

        JwtResponse response = authService.updateRefreshToken(loginResponse.getRefreshToken());

        assertAll(
                () -> assertTrue(jwtProvider.validateAccessToken(response.getAccessToken())),
                () -> assertTrue(jwtProvider.validateRefreshToken(response.getRefreshToken())),
                () -> assertNotEquals(loginResponse.getRefreshToken(), response.getRefreshToken())
        );
    }

    @Test
    void updateRefreshToken_throwsInvalidCredentials_whenOldTokenReused() {
        User user = createUser();
        JwtResponse loginResponse = login(user);
        when(userService.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        authService.updateRefreshToken(loginResponse.getRefreshToken());

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.updateRefreshToken(loginResponse.getRefreshToken()));

        assertEquals("Refresh token has already been used", exception.getMessage());
    }

    @Test
    void updateRefreshToken_newTokenWorks_afterRotation() {
        User user = createUser();
        JwtResponse loginResponse = login(user);
        when(userService.findByUuid(user.getUuid())).thenReturn(Optional.of(user));
        JwtResponse rotated = authService.updateRefreshToken(loginResponse.getRefreshToken());

        JwtResponse response = authService.updateAccessToken(rotated.getRefreshToken());

        assertNotNull(response.getAccessToken());
    }

    @Test
    void getJwtAuthentication_returnsAuthentication_whenContextHasJwt() {
        JwtAuthentication authentication = new JwtAuthentication(List.of(Role.USER), UUID.randomUUID(), true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertSame(authentication, authService.getJwtAuthentication());
    }

    @Test
    void getJwtAuthentication_throwsInvalidCredentials_whenContextIsEmpty() {
        assertThrows(InvalidCredentialsException.class, () -> authService.getJwtAuthentication());
    }

    @Test
    void getJwtAuthentication_throwsInvalidCredentials_whenContextHasOtherAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(UUID.randomUUID(), null, List.of()));

        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.getJwtAuthentication());

        assertEquals("Don't get JwtAuthentication", exception.getMessage());
    }

    private JwtResponse login(User user) {
        when(userService.findByLogin(user.getLogin())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        return authService.authorization(new JwtRequest(user.getLogin(), "12345"));
    }

    private User createUser() {
        return new User(UUID.randomUUID(), "user", "hash", List.of(Role.USER));
    }
}
