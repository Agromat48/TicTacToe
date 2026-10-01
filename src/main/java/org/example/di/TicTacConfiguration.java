package org.example.di;

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.mapper.DataUserMapper;
import org.example.datasource.repository.UserRepository;
import org.example.domain.service.*;
import org.example.web.mapper.WebGameMapper;
import org.example.datasource.repository.GameRepository;
import org.example.web.mapper.WebUserMapper;
import org.springframework.beans.factory.annotation.Value;
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
    public AuthService authService(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        return new AuthService(userService, passwordEncoder, jwtProvider);
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

    @Bean
    public JwtProvider jwtProvider(
            @Value("${jwt.access.secret}") String accessTokenKey,
            @Value("${jwt.refresh.secret}") String refreshTokenKey,
            @Value("${jwt.access.expiration-minutes}") long accessTokenLife,
            @Value("${jwt.refresh.expiration-days}") long refreshTokenLife
            ){
        return new JwtProvider(
                accessTokenKey,
                refreshTokenKey,
                accessTokenLife,
                refreshTokenLife
        );
    }
}