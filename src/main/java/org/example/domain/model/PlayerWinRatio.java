package org.example.domain.model;

import java.util.UUID;

public class PlayerWinRatio {
    private final UUID userUuid;
    private final double winRatio;

    public PlayerWinRatio(UUID userUuid, double winRatio) {
        this.userUuid = userUuid;
        this.winRatio = winRatio;
    }

    public UUID getUserUuid() {
        return userUuid;
    }

    public double getWinRatio() {
        return winRatio;
    }
}
