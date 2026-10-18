package org.example.web.exception;

import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.web.model.response.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameExceptionHandlerTestNotMock {

    private GameExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameExceptionHandler();
    }

    @Test
    void handleInvalidMove_returnsBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidMove(new InvalidMoveException("Not your turn"));

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Not your turn", response.getBody().getMessage());
    }

    @Test
    void handleUserAlreadyExists_returnsConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleUserAlreadyExists(
                new UserAlreadyExistsException("User with login 'user' already exists"));

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("User with login 'user' already exists", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentials_returnsUnauthorized() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(
                new InvalidCredentialsException("Invalid login or password"));

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Invalid login or password", response.getBody().getMessage());
    }
}
