package org.example.datasource.mapper;

import org.example.datasource.model.DataGame;
import org.example.datasource.model.DataGameField;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;

public class DataGameMapper {
    public Game toDomain(DataGame dataGame) {
        return new Game(
                dataGame.getUuid(),
                new GameField(dataGame.getGameField().getMatrix()),
                dataGame.getComputerMark(),
                dataGame.getStatus(),
                dataGame.getCurrentTurnUuid(),
                dataGame.getWinnerUuid(),
                dataGame.getFirstPlayerUuid(),
                dataGame.getSecondPlayerUuid()
                );
    }

    public DataGame toDataSource(Game game) {
        return new DataGame(
                game.getUuid(),
                new DataGameField(game.getGameField().getMatrix()),
                game.getComputerMark(),
                game.getStatus(),
                game.getCurrentTurnUuid(),
                game.getWinnerUuid(),
                game.getFirstPlayerUuid(),
                game.getSecondPlayerUuid()
        );
    }
}
