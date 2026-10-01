package org.example.web.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.service.JwtProvider;
import org.example.domain.util.JwtUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

public class AuthFilter extends GenericFilterBean {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public AuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        String header = httpRequest.getHeader("Authorization");

        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String accessToken = header.substring(BEARER_PREFIX.length());

            if (jwtProvider.validateAccessToken(accessToken)) {
                Claims claims = jwtProvider.getAccessClaims(accessToken);

                JwtAuthentication jwtAuthentication = JwtUtil.getJwtAuthentication(claims);

                SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            }
        }

        chain.doFilter(request, response);
    }
}
