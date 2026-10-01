package org.example.domain.model;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class JwtAuthentication implements Authentication {

    private final List<Role> roles;
    private final UUID uuid;
    private boolean authenticated;

    public JwtAuthentication(List<Role> roles, UUID uuid, boolean authenticated) {
        this.roles = roles;
        this.uuid = uuid;
        this.authenticated = authenticated;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public @Nullable Object getCredentials() {
        return null;
    }

    @Override
    public @Nullable Object getDetails() {
        return null;
    }

    @Override
    public @Nullable Object getPrincipal() {
        return uuid;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return uuid.toString();
    }
}
