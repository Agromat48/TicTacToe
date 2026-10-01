package org.example.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Game {
    public static final UUID COMPUTER_UUID = new UUID(0L, 0L);

    private final UUID uuid;
    private final GameField gameField;
    private final Integer computerMark;

    private final GameStatus status;
    private final UUID currentTurnUuid;
    private final UUID winnerUuid;
    private final UUID firstPlayerUuid;
    private final UUID secondPlayerUuid;
    private final LocalDateTime createdAt;

    public Game(UUID uuid,
                GameField gameField,
                Integer computerMark,
                GameStatus status,
                UUID currentTurnUuid,
                UUID winnerUuid,
                UUID firstPlayerUuid,
                UUID secondPlayerUuid,
                LocalDateTime createdAt
    ) {
        this.uuid = uuid;
        this.gameField = gameField;
        this.computerMark = computerMark;
        this.status = status;
        this.currentTurnUuid = currentTurnUuid;
        this.winnerUuid = winnerUuid;
        this.firstPlayerUuid = firstPlayerUuid;
        this.secondPlayerUuid = secondPlayerUuid;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
