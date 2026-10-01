package org.example.domain.model;

import java.util.List;
import java.util.UUID;


public class User {

    private final UUID uuid;

    private final String login;

    private final String password;

    private final List<Role> roles;

    public User(UUID uuid, String login, String password, List<Role> roles) {
        this.uuid = uuid;
        this.login = login;
        this.password = password;
        this.roles = roles;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public List<Role> getRoles() {
        return roles;
    }
}
