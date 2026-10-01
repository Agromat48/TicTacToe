package org.example.web.model;

import java.util.List;
import java.util.UUID;

public class WebUser {
    private UUID uuid;
    private String login;
    private List<String> roles;

    public WebUser() {
    }

    public WebUser(UUID uuid, String login, List<String> roles) {
        this.uuid = uuid;
        this.login = login;
        this.roles = roles;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}