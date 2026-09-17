package org.example.domain.service;

import org.example.domain.model.Game;
import org.example.domain.model.GameResult;

import java.util.List;
import java.util.UUID;

public interface GameService {
    Game makeNextMove(Game game, int computerMark);
    boolean validation(Game afterGame, Game beforeGame);
    boolean isOver(Game game);
    GameResult getWinner(Game game);
    Game processMove(Game incomingGame, UUID currentUserUuid);
    Game createGame(UUID creatorUuid, boolean vsComputer);
    List<Game> getAvailableGames();
    Game joinGame(UUID gameUuid, UUID joiningUserUuid);
    Game getGame(UUID uuid);
}