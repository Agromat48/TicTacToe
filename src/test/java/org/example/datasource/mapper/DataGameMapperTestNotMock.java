package org.example.datasource.mapper;

import org.example.datasource.model.DataGame;
import org.example.datasource.model.DataGameField;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DataGameMapperTestNotMock {

    private static final int[][] MATRIX = {
            {1, -1, 0},
            {0, 1, 0},
            {-1, 0, 1}
    };

    private DataGameMapper dataGameMapper;

    @BeforeEach
    void setUp() {
        dataGameMapper = new DataGameMapper();
    }

    @Test
    void toDomain_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID currentTurnUuid = UUID.randomUUID();
        UUID winnerUuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        UUID secondPlayerUuid = UUID.randomUUID();
        DataGame dataGame = new DataGame(
                uuid,
                new DataGameField(MATRIX),
                -1,
                GameStatus.WIN,
                currentTurnUuid,
                winnerUuid,
                firstPlayerUuid,
                secondPlayerUuid,
                null
        );

        Game domain = dataGameMapper.toDomain(dataGame);

        assertAll(
                () -> assertEquals(uuid, domain.getUuid()),
                () -> assertArrayEquals(MATRIX, domain.getGameField().getMatrix()),
                () -> assertEquals(-1, domain.getComputerMark()),
                () -> assertEquals(GameStatus.WIN, domain.getStatus()),
                () -> assertEquals(currentTurnUuid, domain.getCurrentTurnUuid()),
                () -> assertEquals(winnerUuid, domain.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, domain.getFirstPlayerUuid()),
                () -> assertEquals(secondPlayerUuid, domain.getSecondPlayerUuid())
        );
    }

    @Test
    void toDomain_keepsNullFields() {
        UUID uuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        DataGame dataGame = new DataGame(
                uuid,
                new DataGameField(new int[3][3]),
                null,
                GameStatus.WAITING_FOR_PLAYERS,
                null,
                null,
                firstPlayerUuid,
                null,
                null
        );

        Game domain = dataGameMapper.toDomain(dataGame);

        assertAll(
                () -> assertEquals(uuid, domain.getUuid()),
                () -> assertNull(domain.getComputerMark()),
                () -> assertEquals(GameStatus.WAITING_FOR_PLAYERS, domain.getStatus()),
                () -> assertNull(domain.getCurrentTurnUuid()),
                () -> assertNull(domain.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, domain.getFirstPlayerUuid()),
                () -> assertNull(domain.getSecondPlayerUuid())
        );
    }

    @Test
    void toDataSource_mapsAllFields() {
        UUID uuid = UUID.randomUUID();
        UUID currentTurnUuid = UUID.randomUUID();
        UUID winnerUuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        UUID secondPlayerUuid = UUID.randomUUID();
        Game game = new Game(
                uuid,
                new GameField(MATRIX),
                -1,
                GameStatus.WIN,
                currentTurnUuid,
                winnerUuid,
                firstPlayerUuid,
                secondPlayerUuid,
                null
        );

        DataGame dataGame = dataGameMapper.toDataSource(game);

        assertAll(
                () -> assertEquals(uuid, dataGame.getUuid()),
                () -> assertArrayEquals(MATRIX, dataGame.getGameField().getMatrix()),
                () -> assertEquals(-1, dataGame.getComputerMark()),
                () -> assertEquals(GameStatus.WIN, dataGame.getStatus()),
                () -> assertEquals(currentTurnUuid, dataGame.getCurrentTurnUuid()),
                () -> assertEquals(winnerUuid, dataGame.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, dataGame.getFirstPlayerUuid()),
                () -> assertEquals(secondPlayerUuid, dataGame.getSecondPlayerUuid())
        );
    }

    @Test
    void toDataSource_keepsNullFields() {
        UUID uuid = UUID.randomUUID();
        UUID firstPlayerUuid = UUID.randomUUID();
        Game game = new Game(
                uuid,
                new GameField(new int[3][3]),
                null,
                GameStatus.WAITING_FOR_PLAYERS,
                null,
                null,
                firstPlayerUuid,
                null,
                null
        );

        DataGame dataGame = dataGameMapper.toDataSource(game);

        assertAll(
                () -> assertEquals(uuid, dataGame.getUuid()),
                () -> assertNull(dataGame.getComputerMark()),
                () -> assertEquals(GameStatus.WAITING_FOR_PLAYERS, dataGame.getStatus()),
                () -> assertNull(dataGame.getCurrentTurnUuid()),
                () -> assertNull(dataGame.getWinnerUuid()),
                () -> assertEquals(firstPlayerUuid, dataGame.getFirstPlayerUuid()),
                () -> assertNull(dataGame.getSecondPlayerUuid())
        );
    }

    @Test
    void roundTrip_gameToDataSourceToDomain_keepsAllFields() {
        Game original = new Game(
                UUID.randomUUID(),
                new GameField(MATRIX),
                -1,
                GameStatus.PLAYER_TURN,
                UUID.randomUUID(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                null
        );

        Game result = dataGameMapper.toDomain(dataGameMapper.toDataSource(original));

        assertAll(
                () -> assertEquals(original.getUuid(), result.getUuid()),
                () -> assertArrayEquals(original.getGameField().getMatrix(), result.getGameField().getMatrix()),
                () -> assertEquals(original.getComputerMark(), result.getComputerMark()),
                () -> assertEquals(original.getStatus(), result.getStatus()),
                () -> assertEquals(original.getCurrentTurnUuid(), result.getCurrentTurnUuid()),
                () -> assertEquals(original.getWinnerUuid(), result.getWinnerUuid()),
                () -> assertEquals(original.getFirstPlayerUuid(), result.getFirstPlayerUuid()),
                () -> assertEquals(original.getSecondPlayerUuid(), result.getSecondPlayerUuid())
        );
    }
}
