package org.example.domain.service;

import org.example.domain.exception.InvalidCredentialsException;
import org.example.domain.exception.UserAlreadyExistsException;
import org.example.domain.model.User;
import org.example.web.model.SignUpRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registration(SignUpRequest signUpRequest) {
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

    public UUID authorization(String header) {
        if (header == null || !header.startsWith("Basic ")) {
            throw new InvalidCredentialsException("Error. Check the entered data");
        }
        String data = header.substring(6);
        String decoded = new String(Base64.getDecoder().decode(data));
        String[] list = decoded.split(":");

        String login = list[0];
        String password = list[1];

        return userService.findByLogin(login)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(User::getUuid)
                .orElseThrow(() -> new InvalidCredentialsException("Error. Check the entered data"));
    }
}
