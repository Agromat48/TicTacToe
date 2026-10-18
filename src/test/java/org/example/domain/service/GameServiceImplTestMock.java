package org.example.domain.service;

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.model.DataGame;
import org.example.datasource.model.DataGameField;
import org.example.datasource.model.DataPlayerWinRatio;
import org.example.datasource.repository.GameRepository;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameStatus;
import org.example.domain.model.PlayerWinRatio;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameServiceImplTestMock {
    @Mock
    private GameRepository repository;

    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(repository, new DataGameMapper());
    }

    @Test
    void createGame_returnsPlayerTurnGame_whenVsComputer() {
        UUID creator = UUID.randomUUID();

        Game game = service.createGame(creator, true);

        assertEquals(-1, game.getComputerMark());
        assertEquals(GameStatus.PLAYER_TURN, game.getStatus());
        assertEquals(creator, game.getCurrentTurnUuid());
        assertEquals(creator, game.getFirstPlayerUuid());
        assertNull(game.getSecondPlayerUuid());
        assertArrayEquals(new int[3][3], game.getGameField().getMatrix());

        ArgumentCaptor<DataGame> captor = ArgumentCaptor.forClass(DataGame.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals(game.getUuid(), captor.getValue().getUuid());
        assertEquals(game.getStatus(), captor.getValue().getStatus());
    }

    @Test
    void createGame_returnsWaitingGame_whenVsHuman() {
        UUID creator = UUID.randomUUID();

        Game game = service.createGame(creator, false);

        assertNull(game.getComputerMark());
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, game.getStatus());
        assertNull(game.getCurrentTurnUuid());
        assertEquals(creator, game.getFirstPlayerUuid());
        assertNull(game.getSecondPlayerUuid());

        ArgumentCaptor<DataGame> captor = ArgumentCaptor.forClass(DataGame.class);
        verify(repository, times(1)).save(captor.capture());
        assertEquals(game.getUuid(), captor.getValue().getUuid());
        assertEquals(GameStatus.WAITING_FOR_PLAYERS, captor.getValue().getStatus());
    }

    @Test
    void getAvailableGames_returnsAllWaitingGames() {
        when(repository.findAllByStatus(GameStatus.WAITING_FOR_PLAYERS))
                .thenReturn(List.of(waitingGame(UUID.randomUUID(), UUID.randomUUID()),
                        waitingGame(UUID.randomUUID(), UUID.randomUUID())));

        List<Game> games = service.getAvailableGames();

        assertEquals(2, games.size());
        verify(repository).findAllByStatus(GameStatus.WAITING_FOR_PLAYERS);
    }

    @Test
    void getAvailableGames_returnsEmptyList_whenNoGames() {
        when(repository.findAllByStatus(GameStatus.WAITING_FOR_PLAYERS)).thenReturn(List.of());

        List<Game> games = service.getAvailableGames();

        assertNotNull(games);
        assertTrue(games.isEmpty());
    }

    @Test
    void getGame_returnsGame_whenGameExists() {
        UUID gameId = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(waitingGame(gameId, UUID.randomUUID())));

        Game game = service.getGame(gameId);

        Assertions.assertEquals(gameId, game.getUuid());
    }

    @Test
    void getGame_throwsInvalidMove_whenGameNotFound() {
        UUID gameId = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.empty());

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.getGame(gameId));

        Assertions.assertEquals("Game not found", invalidMoveException.getMessage());
    }

    @Test
    void joinGame_throwsInvalidMove_whenGameNotFound() {
        UUID gameId = UUID.randomUUID();
        when(repository.findById(gameId)).thenReturn(Optional.empty());

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.joinGame(gameId, UUID.randomUUID()));

        assertEquals("Game not found", invalidMoveException.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void joinGame_throwsInvalidMove_whenJoiningStartedGame() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(twoPlayers(gameId, player1, player2)));

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.joinGame(gameId, UUID.randomUUID()));
        Assertions.assertEquals("Game is not waiting for a second player", invalidMoveException.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void joinGame_throwsNotWaiting_whenOwnGameAlreadyStarted() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(twoPlayers(gameId, player1, player2)));

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.joinGame(gameId, player1));
        Assertions.assertEquals("Game is not waiting for a second player", invalidMoveException.getMessage());
    }

    @Test
    void joinGame_throwsInvalidMove_whenJoiningOwnGame() {
        UUID gameId = UUID.randomUUID();
        UUID creator = UUID.randomUUID();

        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(waitingGame(gameId, creator)));

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.joinGame(gameId, creator));
        Assertions.assertEquals("Cannot join your own game", invalidMoveException.getMessage());

        Mockito.verify(repository, never()).save(any());
    }

    @Test
    void joinGame_assertJoin() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(waitingGame(gameId, player1)));

        Game game = service.joinGame(gameId, player2);

        assertAll(
                () -> assertEquals(gameId, game.getUuid()),
                () -> assertEquals(GameStatus.PLAYER_TURN, game.getStatus()),
                () -> assertEquals(player1, game.getFirstPlayerUuid()),
                () -> assertEquals(player2, game.getSecondPlayerUuid()),
                () -> assertEquals(player1, game.getCurrentTurnUuid()),
                () -> assertNull(game.getWinnerUuid()),
                () -> assertArrayEquals(new int[3][3], game.getGameField().getMatrix())
        );
        ArgumentCaptor<DataGame> captor = ArgumentCaptor.forClass(DataGame.class);
        verify(repository).save(captor.capture());
        assertEquals(GameStatus.PLAYER_TURN, captor.getValue().getStatus());
        assertEquals(player2, captor.getValue().getSecondPlayerUuid());
    }

    @Test
    void getFinishedGames_returnsGamesFromRepository() {
        UUID player = UUID.randomUUID();
        when(repository.findFinishedGamesByUserUuid(player))
                .thenReturn(List.of(waitingGame(UUID.randomUUID(), player)));

        List<Game> games = service.getFinishedGames(player);

        assertEquals(1, games.size());
        verify(repository).findFinishedGamesByUserUuid(player);
    }

    @Test
    void getTopPlayers_mapsRepositoryRows() {
        UUID player = UUID.randomUUID();
        when(repository.findTopPlayersByWinRatio(5)).thenReturn(List.of(winRatio(player, 1.5)));

        List<PlayerWinRatio> top = service.getTopPlayers(5);

        assertEquals(1, top.size());
        assertEquals(player, top.get(0).getUserUuid());
        assertEquals(1.5, top.get(0).getWinRatio());
    }

    @Test
    void processMove_throwsInvalidMove_whenFieldIsNotThreeByThree() {
        Game incoming = incoming(UUID.randomUUID(), new int[][]{{0, 0}, {0, 0}});

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming, UUID.randomUUID()));

        assertEquals("Invalid game field: expected 3x3 matrix", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void processMove_throwsInvalidMove_whenCellHasWrongValue() {
        Game incoming = incoming(UUID.randomUUID(), new int[][]{
                {2, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming, UUID.randomUUID()));

        assertEquals("Invalid game field: cells must be -1, 0 or 1", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void processMove_throwsInvalidMove_whenGameNotFound() {
        UUID gameId = UUID.randomUUID();
        when(repository.findById(gameId)).thenReturn(Optional.empty());

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[3][3]), UUID.randomUUID()));

        assertEquals("Game not found", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenGameIsOver() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] finished = {
                {1, 1, 1},
                {-1, -1, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, finished, player1, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {1, 1, 1},
                        {-1, -1, -1},
                        {0, 0, 0}
                }), player2));

        assertEquals("Game is already over", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenNotPlayersTurn() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, new int[3][3], player1, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {0, 0, 0},
                        {0, -1, 0},
                        {0, 0, 0}
                }), player2));

        assertEquals("Not your turn", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenMoreThanOneCellChanged() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, new int[3][3], player1, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {1, 1, 0},
                        {0, 0, 0},
                        {0, 0, 0}
                }), player1));

        assertEquals("Invalid move: previous cells were changed or move count is not exactly one",
                exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenOldCellOverwritten() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player2, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {-1, 0, 0},
                        {0, 0, 0},
                        {0, 0, 0}
                }), player2));

        assertEquals("Invalid move: previous cells were changed or move count is not exactly one",
                exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenFirstPlayerPlacesO() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, new int[3][3], player1, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {0, 0, 0},
                        {0, -1, 0},
                        {0, 0, 0}
                }), player1));

        assertEquals("Invalid move: wrong mark placed", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_throwsInvalidMove_whenSecondPlayerPlacesX() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player2, player1, player2)));

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> service.processMove(incoming(gameId, new int[][]{
                        {1, 1, 0},
                        {0, 0, 0},
                        {0, 0, 0}
                }), player2));

        assertEquals("Invalid move: wrong mark placed", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    void processMove_botAnswers_whenVsComputerRegularMove() {
        UUID gameId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, new int[3][3], player, player, null)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {0, 0, 0},
                {0, 1, 0},
                {0, 0, 0}
        }), player);

        assertAll(
                () -> assertEquals(2, countMarks(game.getGameField().getMatrix())),
                () -> assertEquals(1, game.getGameField().getMatrix()[1][1]),
                () -> assertEquals(GameStatus.PLAYER_TURN, game.getStatus()),
                () -> assertEquals(player, game.getCurrentTurnUuid()),
                () -> assertEquals(-1, game.getComputerMark()),
                () -> assertNull(game.getWinnerUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_botDoesNotMove_whenPlayerWins() {
        UUID gameId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        int[][] before = {
                {1, 1, 0},
                {-1, -1, 0},
                {0, 0, 0}
        };
        int[][] after = {
                {1, 1, 1},
                {-1, -1, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player, player, null)));

        Game game = service.processMove(incoming(gameId, after), player);

        assertAll(
                () -> assertArrayEquals(after, game.getGameField().getMatrix()),
                () -> assertEquals(GameStatus.WIN, game.getStatus()),
                () -> assertEquals(player, game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_returnsDraw_whenPlayerFillsLastCell() {
        UUID gameId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        int[][] before = {
                {1, -1, 1},
                {1, -1, -1},
                {-1, 1, 0}
        };
        int[][] after = {
                {1, -1, 1},
                {1, -1, -1},
                {-1, 1, 1}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player, player, null)));

        Game game = service.processMove(incoming(gameId, after), player);

        assertAll(
                () -> assertEquals(GameStatus.DRAW, game.getStatus()),
                () -> assertNull(game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_botWins_whenItCompletesLine() {
        UUID gameId = UUID.randomUUID();
        UUID player = UUID.randomUUID();
        int[][] before = {
                {0, -1, -1},
                {1, 0, 0},
                {1, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player, player, null)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {0, -1, -1},
                {1, 1, 0},
                {1, 0, 0}
        }), player);

        assertAll(
                () -> assertEquals(-1, game.getGameField().getMatrix()[0][0]),
                () -> assertEquals(GameStatus.WIN, game.getStatus()),
                () -> assertEquals(Game.COMPUTER_UUID, game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_passesTurnToSecond_whenFirstMoves() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, new int[3][3], player1, player1, player2)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {1, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        }), player1);

        assertAll(
                () -> assertEquals(GameStatus.PLAYER_TURN, game.getStatus()),
                () -> assertEquals(player2, game.getCurrentTurnUuid()),
                () -> assertEquals(1, countMarks(game.getGameField().getMatrix())),
                () -> assertNull(game.getComputerMark())
        );
        assertSaved(game);
    }

    @Test
    void processMove_passesTurnToFirst_whenSecondMoves() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player2, player1, player2)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {1, 0, 0},
                {0, -1, 0},
                {0, 0, 0}
        }), player2);

        assertEquals(GameStatus.PLAYER_TURN, game.getStatus());
        assertEquals(player1, game.getCurrentTurnUuid());
        assertSaved(game);
    }

    @Test
    void processMove_firstPlayerWins() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, 1, 0},
                {-1, -1, 0},
                {0, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player1, player1, player2)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {1, 1, 1},
                {-1, -1, 0},
                {0, 0, 0}
        }), player1);

        assertAll(
                () -> assertEquals(GameStatus.WIN, game.getStatus()),
                () -> assertEquals(player1, game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_secondPlayerWins() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, 1, 0},
                {-1, -1, 0},
                {1, 0, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player2, player1, player2)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {1, 1, 0},
                {-1, -1, -1},
                {1, 0, 0}
        }), player2);

        assertAll(
                () -> assertEquals(GameStatus.WIN, game.getStatus()),
                () -> assertEquals(player2, game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    @Test
    void processMove_returnsDraw_whenHumansFillField() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        int[][] before = {
                {1, -1, 1},
                {1, -1, -1},
                {-1, 1, 0}
        };
        when(repository.findById(gameId))
                .thenReturn(Optional.of(dataGame(gameId, before, player1, player1, player2)));

        Game game = service.processMove(incoming(gameId, new int[][]{
                {1, -1, 1},
                {1, -1, -1},
                {-1, 1, 1}
        }), player1);

        assertAll(
                () -> assertEquals(GameStatus.DRAW, game.getStatus()),
                () -> assertNull(game.getWinnerUuid()),
                () -> assertNull(game.getCurrentTurnUuid())
        );
        assertSaved(game);
    }

    private void assertSaved(Game game) {
        ArgumentCaptor<DataGame> captor = ArgumentCaptor.forClass(DataGame.class);
        verify(repository, times(1)).save(captor.capture());
        DataGame saved = captor.getValue();
        assertAll(
                () -> assertEquals(game.getUuid(), saved.getUuid()),
                () -> assertEquals(game.getStatus(), saved.getStatus()),
                () -> assertEquals(game.getCurrentTurnUuid(), saved.getCurrentTurnUuid()),
                () -> assertEquals(game.getWinnerUuid(), saved.getWinnerUuid()),
                () -> assertArrayEquals(game.getGameField().getMatrix(), saved.getGameField().getMatrix())
        );
    }

    private int countMarks(int[][] matrix) {
        int count = 0;
        for (int[] row : matrix) {
            for (int cell : row) {
                if (cell != 0) {
                    count++;
                }
            }
        }
        return count;
    }

    private Game incoming(UUID gameId, int[][] matrix) {
        return new Game(gameId, new GameField(matrix), null, null, null, null, null, null, null);
    }

    private DataGame dataGame(UUID gameId, int[][] matrix, UUID currentTurn, UUID firstPlayer, UUID secondPlayer) {
        return new DataGame(
                gameId,
                new DataGameField(matrix),
                secondPlayer == null ? -1 : null,
                GameStatus.PLAYER_TURN,
                currentTurn,
                null,
                firstPlayer,
                secondPlayer,
                null
        );
    }

    private DataPlayerWinRatio winRatio(UUID userUuid, double ratio) {
        return new DataPlayerWinRatio() {
            @Override
            public UUID getUserUuid() {
                return userUuid;
            }

            @Override
            public Double getWinRatio() {
                return ratio;
            }
        };
    }

    private DataGame waitingGame(UUID gameId, UUID firstPlayer) {
        return new DataGame(
                gameId,
                new DataGameField(new int[3][3]),
                null,
                GameStatus.WAITING_FOR_PLAYERS,
                null,
                null,
                firstPlayer,
                null,
                null
        );
    }

    private DataGame twoPlayers(UUID gameId, UUID firstPlayer, UUID secondPlayer) {
        return new DataGame(
                gameId,
                new DataGameField(new int[3][3]),
                null,
                GameStatus.PLAYER_TURN,
                firstPlayer,
                null,
                firstPlayer,
                secondPlayer,
                null
        );
    }
}
