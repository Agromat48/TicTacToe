package org.example.web.controller;

import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.web.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GameExceptionHandler {

    @ExceptionHandler(InvalidMoveException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMove(InvalidMoveException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlredyExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
    }
}