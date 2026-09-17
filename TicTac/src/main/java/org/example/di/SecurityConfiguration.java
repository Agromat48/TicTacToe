package org.example.di;

import org.example.domain.service.AuthService;
import org.example.web.filter.AuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    @Bean
    public AuthFilter authFilter(AuthService authService) {
        return new AuthFilter(authService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthFilter authFilter) {
        http.csrf(AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(path -> {
            path.requestMatchers("/auth/sign_up", "/auth/login").permitAll();
            path.anyRequest().authenticated();
        });

        http.addFilterBefore(authFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
