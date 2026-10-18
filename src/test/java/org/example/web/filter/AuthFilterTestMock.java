package org.example.web.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.Role;
import org.example.domain.service.JwtProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthFilterTestMock {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    private AuthFilter authFilter;

    @BeforeEach
    void setUp() {
        authFilter = new AuthFilter(jwtProvider);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_passesWithoutAuthentication_whenHeaderIsMissing() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtProvider);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_passesWithoutAuthentication_whenHeaderIsBasic() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtProvider);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_passesWithoutAuthentication_whenPrefixIsLowercase() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("bearer token");

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verifyNoInteractions(jwtProvider);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_passesWithoutAuthentication_whenTokenIsInvalid() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtProvider.validateAccessToken("bad-token")).thenReturn(false);

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        verify(jwtProvider, never()).getAccessClaims(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilter_setsAuthentication_whenTokenIsValid() throws Exception {
        UUID uuid = UUID.randomUUID();
        Claims claims = Jwts.claims()
                .add("uuid", uuid.toString())
                .add("roles", List.of("USER"))
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer good-token");
        when(jwtProvider.validateAccessToken("good-token")).thenReturn(true);
        when(jwtProvider.getAccessClaims("good-token")).thenReturn(claims);

        authFilter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        JwtAuthentication jwtAuthentication = assertInstanceOf(JwtAuthentication.class, authentication);
        assertEquals(uuid, jwtAuthentication.getPrincipal());
        assertEquals(List.of(Role.USER), jwtAuthentication.getRoles());
        assertTrue(jwtAuthentication.isAuthenticated());
    }
}
