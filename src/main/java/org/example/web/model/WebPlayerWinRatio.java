package org.example.web.model;

import java.util.UUID;

public class WebPlayerWinRatio {
    private UUID uuid;
    private String login;
    private double winRatio;

    public WebPlayerWinRatio() {
    }

    public WebPlayerWinRatio(UUID uuid, String login, double winRatio) {
        this.uuid = uuid;
        this.login = login;
        this.winRatio = winRatio;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLogin() {
        return login;
    }

    public double getWinRatio() {
        return winRatio;
    }
}
