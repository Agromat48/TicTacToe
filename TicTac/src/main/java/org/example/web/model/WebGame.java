package org.example.web.model;

import org.example.domain.model.GameStatus;

import java.util.UUID;

public class WebGame {
    private UUID uuid;
    private WebGameField gameField;
    private GameStatus status;
    private UUID currentTurnUuid;
    private UUID winnerUuid;
    private UUID firstPlayerUuid;
    private UUID secondPlayerUuid;
    private int firstPlayerMark;
    private int secondPlayerMark;

    public WebGame() {
    }

    public WebGame(UUID uuid) {
        this.uuid = uuid;
        this.gameField = new WebGameField();
    }

    public WebGame(UUID uuid,
                   WebGameField gameField,
                   GameStatus status,
                   UUID currentTurnUuid,
                   UUID winnerUuid,
                   UUID firstPlayerUuid,
                   UUID secondPlayerUuid,
                   int firstPlayerMark,
                   int secondPlayerMark
    ) {
        this.uuid = uuid;
        this.gameField = gameField;
        this.status = status;
        this.currentTurnUuid = currentTurnUuid;
        this.winnerUuid = winnerUuid;
        this.firstPlayerUuid = firstPlayerUuid;
        this.secondPlayerUuid = secondPlayerUuid;
        this.firstPlayerMark = firstPlayerMark;
        this.secondPlayerMark = secondPlayerMark;
    }

    public UUID getUuid() {
        return uuid;
    }

    public WebGameField getGameField() {
        return gameField;
    }

    public GameStatus getStatus() {
        return status;
    }

    public UUID getCurrentTurnUuid() {
        return currentTurnUuid;
    }

    public UUID getWinnerUuid() {
        return winnerUuid;
    }

    public UUID getFirstPlayerUuid() {
        return firstPlayerUuid;
    }

    public UUID getSecondPlayerUuid() {
        return secondPlayerUuid;
    }

    public int getFirstPlayerMark() {
        return firstPlayerMark;
    }

    public int getSecondPlayerMark() {
        return secondPlayerMark;
    }
}