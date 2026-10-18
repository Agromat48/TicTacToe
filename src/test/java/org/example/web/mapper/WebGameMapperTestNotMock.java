package org.example.web.mapper;

import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameStatus;
import org.example.web.model.WebGame;
import org.example.web.model.WebGameField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class WebGameMapperTestNotMock {

    private static final int[][] MATRIX = {
            {1, -1, 0},
            {0, 1, 0},
            {-1, 0, 1}
    };

    private WebGameMapper webGameMapper;

    @BeforeEach
    void setUp() {
        webGameMapper = new WebGameMapper();
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID currentTurnUuid = UUID.randomUUID();
        UUID winnerUuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        UUID secondPlayerUuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        WebGame webGame = new WebGame(
                uuid,
                new WebGameField(MATRIX),
                GameStatus.WIN,
                currentTurnUuid,
                winnerUuid,
                firstPlayerUuid,
                secondPlayerUuid,
                1,
                -1,
                createdAt
        );

        Game game = webGameMapper.toDomain(webGame);

        assertAll(
                () -> assertEquals(uuid, game.getUuid()),
                () -> assertArrayEquals(MATRIX, game.getGameField().getMatrix()),
                () -> assertEquals(GameStatus.WIN, game.getStatus()),
                () -> assertEquals(currentTurnUuid, game.getCurrentTurnUuid()),
                () -> assertEquals(winnerUuid, game.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, game.getFirstPlayerUuid()),
                () -> assertEquals(secondPlayerUuid, game.getSecondPlayerUuid()),
                () -> assertEquals(createdAt, game.getCreatedAt())
        );
    }

    @Test
    void toDomain_computerMarkIsAlwaysNull() {
        WebGame webGame = new WebGame(UUID.randomUUID(), new WebGameField(MATRIX), GameStatus.PLAYER_TURN,
                null, null, UUID.randomUUID(), null, 1, -1, null);

        Game game = webGameMapper.toDomain(webGame);

        assertNull(game.getComputerMark());
    }

    @Test
    void toDomain_keepsNullMatrix_whenGameFieldIsNull() {
        WebGame webGame = new WebGame();

        Game game = webGameMapper.toDomain(webGame);

        assertNull(game.getGameField().getMatrix());
    }

    @Test
    void toWeb_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID currentTurnUuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        UUID secondPlayerUuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        Game game = new Game(
                uuid,
                new GameField(MATRIX),
                null,
                GameStatus.PLAYER_TURN,
                currentTurnUuid,
                null,
                firstPlayerUuid,
                secondPlayerUuid,
                createdAt
        );

        WebGame webGame = webGameMapper.toWeb(game);

        assertAll(
                () -> assertEquals(uuid, webGame.getUuid()),
                () -> assertArrayEquals(MATRIX, webGame.getGameField().getMatrix()),
                () -> assertEquals(GameStatus.PLAYER_TURN, webGame.getStatus()),
                () -> assertEquals(currentTurnUuid, webGame.getCurrentTurnUuid()),
                () -> assertNull(webGame.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, webGame.getFirstPlayerUuid()),
                () -> assertEquals(secondPlayerUuid, webGame.getSecondPlayerUuid()),
                () -> assertEquals(createdAt, webGame.getCreatedAt())
        );
    }

    @Test
    void toWeb_setsPlayerMarks() {
        Game game = new Game(UUID.randomUUID(), new GameField(), null, GameStatus.PLAYER_TURN,
                null, null, UUID.randomUUID(), UUID.randomUUID(), null);

        WebGame webGame = webGameMapper.toWeb(game);

        assertEquals(1, webGame.getFirstPlayerMark());
        assertEquals(-1, webGame.getSecondPlayerMark());
    }

    @Test
    void toWeb_doesNotFail_whenSecondPlayerIsNull() {
        UUID firstPlayerUuid = UUID.randomUUID();
        Game game = new Game(UUID.randomUUID(), new GameField(), -1, GameStatus.PLAYER_TURN,
                firstPlayerUuid, null, firstPlayerUuid, null, null);

        WebGame webGame = webGameMapper.toWeb(game);

        assertEquals(firstPlayerUuid, webGame.getFirstPlayerUuid());
        assertNull(webGame.getSecondPlayerUuid());
    }
}
