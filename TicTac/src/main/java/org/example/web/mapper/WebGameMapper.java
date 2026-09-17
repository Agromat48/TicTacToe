package org.example.web.mapper;

import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.web.model.WebGame;
import org.example.web.model.WebGameField;

public class WebGameMapper {
    private static final int FIRST_PLAYER_MARK = 1;
    private static final int SECOND_PLAYER_MARK = -1;

    public Game toDomain(WebGame webGame) {
        return new Game(
                webGame.getUuid(),
                new GameField(webGame.getGameField().getMatrix()),
                null,
                webGame.getStatus(),
                webGame.getCurrentTurnUuid(),
                webGame.getWinnerUuid(),
                webGame.getFirstPlayerUuid(),
                webGame.getSecondPlayerUuid()
        );
    }

    public WebGame toWeb(Game game) {
        return new WebGame(
                game.getUuid(),
                new WebGameField(game.getGameField().getMatrix()),
                game.getStatus(),
                game.getCurrentTurnUuid(),
                game.getWinnerUuid(),
                game.getFirstPlayerUuid(),
                game.getSecondPlayerUuid(),
                FIRST_PLAYER_MARK,
                SECOND_PLAYER_MARK
        );
    }
}