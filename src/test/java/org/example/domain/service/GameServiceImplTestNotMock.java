package org.example.domain.service;

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.repository.GameRepository;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameResult;
import org.example.domain.model.GameStatus;
import org.example.domain.service.GameServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

public class GameServiceImplTestNotMock {
    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(mock(GameRepository.class),
                new DataGameMapper());
    }

    @Test
    void getWinner_returnsXWins_whenRowFieldWithX() {
        Game game = gameWith(new int[][]{
                {1, 1, 1},
                {-1, -1, 0},
                {0, 0, 0}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.X_WINS, winner);
    }

    @Test
    void getWinner_returnsOWins_whenColmFiledWithO() {
        Game game = gameWith(new int[][]{
                {-1, 1, 0},
                {-1, 1, 1},
                {-1, 0, 0}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.O_WINS, winner);
    }

    @Test
    void getWinner_returnXWins_whenDio1FieldWithX() {
        Game game = gameWith(new int[][]{
                {1, -1, 0},
                {-1, 1, 0},
                {-1, 0, 1}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.X_WINS, winner);
    }

    @Test
    void getWinner_returnXWins_whenDio2FieldWithX() {
        Game game = gameWith(new int[][]{
                {-1, -1, 1},
                {-1, 1, 0},
                {1, 0, 0}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.X_WINS, winner);
    }

    @Test
    void getWinner_returnDraw() {
        Game game = gameWith(new int[][]{
                {1, -1, 1},
                {-1, 1, 1},
                {-1, 1, -1}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.DRAW, winner);
    }

    @Test
    void getWinner_returnNotFinished() {
        Game game = gameWith(new int[][]{
                {-1, -1, 1},
                {-1, 1, 0},
                {0, 0, 0}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.NOT_FINISHED, winner);
    }

    @Test
    void getWinner_returnNotFinished_whenFiledIsEmpty() {
        Game game = gameWith(new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 0, 0}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.NOT_FINISHED, winner);
    }

    @Test
    void getWinner_returnNotFinished_whenLastStep() {
        Game game = gameWith(new int[][]{
                {1, 0, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.NOT_FINISHED, winner);
    }

    @Test
    void getWinner_returnXWins_whenLastStep() {
        Game game = gameWith(new int[][]{
                {1, 1, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });

        GameResult winner = service.getWinner(game);

        Assertions.assertEquals(GameResult.X_WINS, winner);
    }

    @Test
    void isOver_trueX() {
        Game game = gameWith(new int[][]{
                {1, 1, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });

        boolean over = service.isOver(game);

        Assertions.assertTrue(over);
    }

    @Test
    void isOver_falseX() {
        Game game = gameWith(new int[][]{
                {1, 0, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });

        boolean over = service.isOver(game);

        Assertions.assertFalse(over);
    }

    @Test
    void isOver_trueO() {
        Game game = gameWith(new int[][]{
                {-1, 0, 1},
                {-1, 1, 0},
                {-1, 1, 0}
        });

        boolean over = service.isOver(game);

        Assertions.assertTrue(over);
    }

    @Test
    void isOver_falseO() {
        Game game = gameWith(new int[][]{
                {0, 0, 1},
                {-1, 1, 0},
                {-1, 1, 0}
        });

        boolean over = service.isOver(game);

        Assertions.assertFalse(over);
    }

    @Test
    void validation_trueX_whenOneStep() {
        Game beforeGame = gameWith(new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 1, 0}
        });

        Game afterGame = gameWith(new int[][]{
                {0, 0, 0},
                {0, -1, 0},
                {0, 1, 0}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertTrue(over);
    }

    @Test
    void validation_trueX_whenLastSteps() {
        Game beforeGame = gameWith(new int[][]{
                {1, 0, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });

        Game afterGame = gameWith(new int[][]{
                {1, 1, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertTrue(over);
    }

    @Test
    void validation_falseX_whenOneStep() {
        Game beforeGame = gameWith(new int[][]{
                {0, 0, 0},
                {0, 0, 0},
                {0, 1, 0}
        });

        Game afterGame = gameWith(new int[][]{
                {0, 0, 0},
                {0, -1, 0},
                {0, -1, 0}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertFalse(over);
    }

    @Test
    void validation_falseX_whenCountMoreOne() {
        Game beforeGame = gameWith(new int[][]{
                {0, 0, 1},
                {-1, 1, -1},
                {-1, 1, 0}
        });

        Game afterGame = gameWith(new int[][]{
                {1, 0, 1},
                {-1, 1, -1},
                {-1, 1, -1}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertFalse(over);
    }

    @Test
    void validation_trueO_whenOneStep() {
        Game beforeGame = gameWith(new int[][]{
                {0, 0, 0},
                {0, 1, 0},
                {-1, 1, 0}
        });

        Game afterGame = gameWith(new int[][]{
                {0, 0, 0},
                {-1, 1, 0},
                {-1, 1, 0}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertTrue(over);
    }

    @Test
    void validation_trueO_whenLastSteps() {
        Game beforeGame = gameWith(new int[][]{
                {0, 0, 0},
                {-1, 1, 0},
                {-1, 1, 0}
        });

        Game afterGame = gameWith(new int[][]{
                {-1, 0, 0},
                {-1, 1, 0},
                {-1, 1, 0}
        });


        boolean over = service.validation(afterGame, beforeGame);

        Assertions.assertTrue(over);
    }

    private Game gameWith(int[][] m) {
        return new Game(UUID.randomUUID(), new GameField(m), null,
                GameStatus.PLAYER_TURN, null, null, null, null, null);
    }
}

