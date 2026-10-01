package org.example.web.controller;

import org.example.domain.model.JwtRequest;
import org.example.domain.model.JwtResponse;
import org.example.domain.service.AuthService;
import org.example.domain.model.SignUpRequest;
import org.example.web.model.request.RefreshJwtRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public JwtResponse authorization(@RequestBody JwtRequest request) {
        return authService.authorization(request);
    }

    @PostMapping("/auth/token")
    public JwtResponse updateAccessToken(@RequestBody RefreshJwtRequest request) {
        return authService.updateAccessToken(request.getRefreshToken());
    }

    @PostMapping("/auth/refresh")
    public JwtResponse updateRefreshToken(@RequestBody RefreshJwtRequest request) {
        return authService.updateRefreshToken(request.getRefreshToken());
    }

}
