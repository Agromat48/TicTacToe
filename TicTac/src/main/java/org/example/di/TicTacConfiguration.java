package org.example.di;

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.mapper.DataUserMapper;
import org.example.datasource.repository.UserRepository;
import org.example.domain.service.AuthService;
import org.example.domain.service.UserService;
import org.example.web.mapper.WebGameMapper;
import org.example.datasource.repository.GameRepository;
import org.example.domain.service.GameServiceImpl;
import org.example.domain.service.GameService;
import org.example.web.mapper.WebUserMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class TicTacConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthService authService(UserService userService, PasswordEncoder passwordEncoder) {
        return new AuthService(userService, passwordEncoder);
    }

    @Bean
    public DataUserMapper dataUserMapper() {
        return new DataUserMapper();
    }

    @Bean
    public UserService userService(UserRepository userRepository, DataUserMapper dataUserMapper) {
        return new UserService(userRepository, dataUserMapper);
    }

    @Bean
    public DataGameMapper dataGameMapper() {
        return new DataGameMapper();
    }

    @Bean
    public WebGameMapper webGameMapper() {
        return new WebGameMapper();
    }

    @Bean
    public WebUserMapper webUserMapper() {
        return new WebUserMapper();
    }

    @Bean
    public GameService gameService(GameRepository gameRepository, DataGameMapper dataGameMapper) {
        return new GameServiceImpl(gameRepository, dataGameMapper);
    }
}