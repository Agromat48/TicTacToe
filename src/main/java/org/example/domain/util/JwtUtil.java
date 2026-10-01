package org.example.domain.util;

import io.jsonwebtoken.Claims;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.Role;

import java.util.List;
import java.util.UUID;

public class JwtUtil {
    private JwtUtil() {}

    public static JwtAuthentication getJwtAuthentication(Claims claims) {
        List<?> rawRoles = claims.get("roles", List.class);
        List<Role> roles = rawRoles.stream()
                .map(el -> Role.valueOf(el.toString()))
                .toList();

        UUID uuid = UUID.fromString(claims.get("uuid", String.class));

        return new JwtAuthentication(roles, uuid, true);
    }
}
