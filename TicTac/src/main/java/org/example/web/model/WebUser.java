package org.example.web.model;

import java.util.UUID;

public class WebUser {
    private UUID uuid;
    private String login;

    public WebUser() {
    }

    public WebUser(UUID uuid, String login) {
        this.uuid = uuid;
        this.login = login;
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
}