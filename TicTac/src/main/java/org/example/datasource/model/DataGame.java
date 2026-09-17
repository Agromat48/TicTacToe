package org.example.datasource.model;

import jakarta.persistence.*;
import org.example.domain.model.GameStatus;

import java.util.UUID;

@Entity
@Table(name = "game")
public class DataGame {

    @Id
    private UUID uuid;

    @Convert(converter = GameFieldConverter.class)
    private DataGameField gameField;

    @Column(name = "computer_mark")
    private Integer computerMark;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status;

    @Column(name = "current_turn_uuid")
    private UUID currentTurnUuid;

    @Column(name = "winner_uuid")
    private UUID winnerUuid;

    @Column(name = "first_player_uuid")
    private UUID firstPlayerUuid;

    @Column(name = "second_player_uuid")
    private UUID secondPlayerUuid;

    public DataGame() {
    }

    public DataGame(UUID uuid,
                DataGameField gameField,
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

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public DataGameField getGameField() {
        return gameField;
    }

    public void setGameField(DataGameField gameField) {
        this.gameField = gameField;
    }

    public Integer getComputerMark() {
        return computerMark;
    }

    public void setComputerMark(Integer computerMark) {
        this.computerMark = computerMark;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public UUID getCurrentTurnUuid() {
        return currentTurnUuid;
    }

    public void setCurrentTurnUuid(UUID currentTurnUuid) {
        this.currentTurnUuid = currentTurnUuid;
    }

    public UUID getWinnerUuid() {
        return winnerUuid;
    }

    public void setWinnerUuid(UUID winnerUuid) {
        this.winnerUuid = winnerUuid;
    }

    public UUID getFirstPlayerUuid() {
        return firstPlayerUuid;
    }

    public void setFirstPlayerUuid(UUID firstPlayerUuid) {
        this.firstPlayerUuid = firstPlayerUuid;
    }

    public UUID getSecondPlayerUuid() {
        return secondPlayerUuid;
    }

    public void setSecondPlayerUuid(UUID secondPlayerUuid) {
        this.secondPlayerUuid = secondPlayerUuid;
    }
}
