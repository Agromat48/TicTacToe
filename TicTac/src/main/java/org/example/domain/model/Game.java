package org.example.domain.model;

import java.util.UUID;

public class Game {
    private final UUID uuid;
    private final GameField gameField;
    private final Integer computerMark;

    private final GameStatus status;
    private final UUID currentTurnUuid;
    private final UUID winnerUuid;
    private final UUID firstPlayerUuid;
    private final UUID secondPlayerUuid;

    public Game(UUID uuid,
                GameField gameField,
                Integer computerMark,
                GameStatus status,
                UUID currentTurnUuid,
                UUID winnerUuid,
                UUID firstPlayerUuid,
                UUID secondPlayerUuid
    ) {
        this.uuid = uuid;
        this.gameField = gameField;
        this.computerMark = computerMark;
        this.status = status;
        this.currentTurnUuid = currentTurnUuid;
        this.winnerUuid = winnerUuid;
        this.firstPlayerUuid = firstPlayerUuid;
        this.secondPlayerUuid = secondPlayerUuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public GameField getGameField() {
        return gameField;
    }

    public Integer getComputerMark() {
        return computerMark;
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
}
