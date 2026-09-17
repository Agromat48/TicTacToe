package org.example.domain.service;

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.model.DataGame;
import org.example.datasource.repository.GameRepository;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameResult;
import org.example.domain.model.GameStatus;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GameServiceImpl implements GameService {
    private static final int X_MARK = 1;
    private static final int O_MARK = -1;

    private final GameRepository gameRepository;
    private final DataGameMapper dataGameMapper;

    public GameServiceImpl(GameRepository gameRepository, DataGameMapper gameMapper) {
        this.gameRepository = gameRepository;
        this.dataGameMapper = gameMapper;
    }

    @Override
    public Game createGame(UUID creatorUuid, boolean vsComputer) {
        Game createdGame = new Game(
                UUID.randomUUID(),
                new GameField(),
                vsComputer ? O_MARK : null,
                vsComputer ? GameStatus.PLAYER_TURN : GameStatus.WAITING_FOR_PLAYERS,
                vsComputer ? creatorUuid : null,
                null,
                creatorUuid,
                null
        );

        gameRepository.save(dataGameMapper.toDataSource(createdGame));
        return createdGame;
    }

    @Override
    public List<Game> getAvailableGames() {
        return gameRepository
                .findAllByStatus(GameStatus.WAITING_FOR_PLAYERS)
                .stream()
                .map(dataGameMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Game joinGame(UUID gameUuid, UUID joiningUserUuid) {
        Game oldGame = findGameOrThrow(gameUuid);

        if (oldGame.getStatus() != GameStatus.WAITING_FOR_PLAYERS) {
            throw new InvalidMoveException("Game is not waiting for a second player");
        }

        if (joiningUserUuid.equals(oldGame.getFirstPlayerUuid())) {
            throw new InvalidMoveException("Cannot join your own game");
        }

        Game updatedGame = new Game(
                oldGame.getUuid(),
                oldGame.getGameField(),
                oldGame.getComputerMark(),
                GameStatus.PLAYER_TURN,
                oldGame.getFirstPlayerUuid(),
                null,
                oldGame.getFirstPlayerUuid(),
                joiningUserUuid
        );

        gameRepository.save(dataGameMapper.toDataSource(updatedGame));
        return updatedGame;
    }

    @Override
    public Game getGame(UUID uuid) {
        return findGameOrThrow(uuid);
    }

    @Override
    public Game processMove(Game incomingGame, UUID currentUserUuid) {
        Game oldGame = findGameOrThrow(incomingGame.getUuid());

        if (isOver(oldGame)) {
            throw new InvalidMoveException("Game is already over");
        }

        if (!currentUserUuid.equals(oldGame.getCurrentTurnUuid())) {
            throw new InvalidMoveException("Not your turn");
        }

        if (!validation(incomingGame, oldGame)) {
            throw new InvalidMoveException("Invalid move: previous cells were changed or move count is not exactly one");
        }

        validateMoveMark(incomingGame, oldGame, currentUserUuid);

        boolean vsComputer = oldGame.getSecondPlayerUuid() == null;

        Game savedGame = vsComputer
                ? handleBotTurn(incomingGame, oldGame)
                : handleHumanTurn(incomingGame, oldGame, currentUserUuid);

        gameRepository.save(dataGameMapper.toDataSource(savedGame));
        return savedGame;
    }

    private Game handleBotTurn(Game incomingGame, Game oldGame) {
        Game gameAfterPlayerMove = new Game(
                incomingGame.getUuid(),
                incomingGame.getGameField(),
                O_MARK,
                null, null, null,
                oldGame.getFirstPlayerUuid(),
                oldGame.getSecondPlayerUuid()
        );

        if (isOver(gameAfterPlayerMove)) {
            return finishGame(gameAfterPlayerMove, O_MARK);
        }

        return makeNextMove(gameAfterPlayerMove, O_MARK);
    }

    private Game handleHumanTurn(Game incomingGame, Game oldGame, UUID currentUserUuid) {
        UUID opponentUuid = currentUserUuid.equals(oldGame.getFirstPlayerUuid())
                ? oldGame.getSecondPlayerUuid()
                : oldGame.getFirstPlayerUuid();

        Game gameAfterMove = new Game(
                incomingGame.getUuid(),
                incomingGame.getGameField(),
                null,
                null,
                null,
                null,
                oldGame.getFirstPlayerUuid(),
                oldGame.getSecondPlayerUuid()
        );

        if (isOver(gameAfterMove)) {
            return finishGame(gameAfterMove, null);
        }

        return new Game(
                gameAfterMove.getUuid(),
                gameAfterMove.getGameField(),
                null,
                GameStatus.PLAYER_TURN,
                opponentUuid,
                null,
                gameAfterMove.getFirstPlayerUuid(),
                gameAfterMove.getSecondPlayerUuid()
        );
    }

    private Game finishGame(Game game, Integer computerMark) {
        GameStatus status = resolveStatus(game);

        return new Game(
                game.getUuid(),
                game.getGameField(),
                computerMark,
                status,
                null,
                status == GameStatus.WIN ? resolveWinnerUuid(game) : null,
                game.getFirstPlayerUuid(),
                game.getSecondPlayerUuid()
        );
    }

    private void validateMoveMark(Game incomingGame, Game oldGame, UUID currentUserUuid) {
        int expectedMark = currentUserUuid.equals(oldGame.getFirstPlayerUuid()) ? X_MARK : O_MARK;

        int[][] after = incomingGame.getGameField().getMatrix();
        int[][] before = oldGame.getGameField().getMatrix();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (before[i][j] == 0 && after[i][j] != 0 && after[i][j] != expectedMark) {
                    throw new InvalidMoveException("Invalid move: wrong mark placed");
                }
            }
        }
    }

    private Game findGameOrThrow(UUID uuid) {
        DataGame dataGame = gameRepository.findById(uuid)
                .orElseThrow(() -> new InvalidMoveException("Game not found"));
        return dataGameMapper.toDomain(dataGame);
    }

    @Override
    public Game makeNextMove(Game game, int computerMark) {
        int[][] matrix = copyMatrix(game.getGameField().getMatrix());

        int bestScore = Integer.MIN_VALUE;
        int bestI = -1;
        int bestJ = -1;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][j] = computerMark;
                    int score = minimax(matrix, false, computerMark);
                    matrix[i][j] = 0;

                    if (score > bestScore) {
                        bestScore = score;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
        }

        if (bestI == -1) {
            GameStatus status = resolveStatus(game);
            return new Game(
                    game.getUuid(),
                    game.getGameField(),
                    computerMark,
                    status,
                    status == GameStatus.PLAYER_TURN ? game.getFirstPlayerUuid() : null,
                    status == GameStatus.WIN ? resolveWinnerUuid(game) : null,
                    game.getFirstPlayerUuid(),
                    game.getSecondPlayerUuid()
            );
        }

        int[][] newMatrix = copyMatrix(game.getGameField().getMatrix());
        newMatrix[bestI][bestJ] = computerMark;

        Game gameAfterBotMove = new Game(
                game.getUuid(),
                new GameField(newMatrix),
                computerMark,
                null, null, null,
                game.getFirstPlayerUuid(),
                game.getSecondPlayerUuid()
        );

        GameStatus status = resolveStatus(gameAfterBotMove);

        return new Game(
                gameAfterBotMove.getUuid(),
                gameAfterBotMove.getGameField(),
                computerMark,
                status,
                status == GameStatus.PLAYER_TURN ? gameAfterBotMove.getFirstPlayerUuid() : null,
                status == GameStatus.WIN ? resolveWinnerUuid(gameAfterBotMove) : null,
                gameAfterBotMove.getFirstPlayerUuid(),
                gameAfterBotMove.getSecondPlayerUuid()
        );
    }

    private GameStatus resolveStatus(Game game) {
        GameResult result = getWinner(game);

        if (result == GameResult.NOT_FINISHED) {
            return GameStatus.PLAYER_TURN;
        }
        if (result == GameResult.DRAW) {
            return GameStatus.DRAW;
        }
        return GameStatus.WIN;
    }

    private UUID resolveWinnerUuid(Game game) {
        GameResult result = getWinner(game);

        if (result == GameResult.X_WINS) {
            return game.getFirstPlayerUuid();
        }
        if (result == GameResult.O_WINS) {
            return game.getSecondPlayerUuid();
        }
        return null;
    }

    private int minimax(int[][] matrix, boolean isMaximizing, int computerMark) {
        GameResult result = getWinner(matrix);

        if (result != GameResult.NOT_FINISHED) {
            if (result == GameResult.DRAW) {
                return 0;
            }
            boolean computerWon = (result == GameResult.X_WINS && computerMark == 1)
                    || (result == GameResult.O_WINS && computerMark == -1);
            return computerWon ? 1 : -1;
        }

        int mark = isMaximizing ? computerMark : -computerMark;
        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (matrix[i][j] == 0) {
                    matrix[i][j] = mark;
                    int score = minimax(matrix, !isMaximizing, computerMark);
                    matrix[i][j] = 0;

                    bestScore = isMaximizing
                            ? Math.max(bestScore, score)
                            : Math.min(bestScore, score);
                }
            }
        }

        return bestScore;
    }

    @Override
    public boolean validation(Game afterGame, Game beforeGame) {
        int[][] after = afterGame.getGameField().getMatrix();
        int[][] before = beforeGame.getGameField().getMatrix();

        int count = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (before[i][j] != 0 && before[i][j] != after[i][j]) {
                    return false;
                }
                if (before[i][j] == 0 && after[i][j] != 0) {
                    count++;
                }
            }
        }

        return count == 1;
    }

    @Override
    public boolean isOver(Game game) {
        return getWinner(game) != GameResult.NOT_FINISHED;
    }

    @Override
    public GameResult getWinner(Game game) {
        return getWinner(game.getGameField().getMatrix());
    }

    private int[][] copyMatrix(int[][] matrix) {
        int[][] copy = new int[3][3];
        for (int i = 0; i < 3; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    private GameResult getWinner(int[][] matrix) {
        for (int i = 0; i < 3; i++) {
            GameResult result = checkLine(matrix[i][0], matrix[i][1], matrix[i][2]);
            if (result != null) return result;
        }

        for (int j = 0; j < 3; j++) {
            GameResult result = checkLine(matrix[0][j], matrix[1][j], matrix[2][j]);
            if (result != null) return result;
        }

        GameResult diag1 = checkLine(matrix[0][0], matrix[1][1], matrix[2][2]);
        if (diag1 != null) return diag1;

        GameResult diag2 = checkLine(matrix[0][2], matrix[1][1], matrix[2][0]);
        if (diag2 != null) return diag2;

        for (int[] row : matrix) {
            for (int cell : row) {
                if (cell == 0) {
                    return GameResult.NOT_FINISHED;
                }
            }
        }

        return GameResult.DRAW;
    }

    private GameResult checkLine(int a, int b, int c) {
        int sum = a + b + c;
        if (sum == 3) return GameResult.X_WINS;
        if (sum == -3) return GameResult.O_WINS;
        return null;
    }
}