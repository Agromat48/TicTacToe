package org.example.web.controller;

import org.example.domain.service.AuthService;
import org.example.web.model.SignUpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/sign_up")
    public ResponseEntity<Void> registration(@RequestBody SignUpRequest signUpRequest) {
        if(authService.registration(signUpRequest)) {
            return ResponseEntity.ok().build();
        }
        return  ResponseEntity.badRequest().build();
    }

    @PostMapping("/auth/login")
    public UUID authorization(@RequestHeader("Authorization") String header) {
        return authService.authorization(header);
    }
}
