package org.example.web.controller;

import org.example.domain.exception.InvalidMoveException;
import org.example.domain.model.Game;
import org.example.domain.model.GameField;
import org.example.domain.model.GameStatus;
import org.example.domain.model.JwtAuthentication;
import org.example.domain.model.PlayerWinRatio;
import org.example.domain.model.Role;
import org.example.domain.model.User;
import org.example.domain.service.AuthService;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.example.web.mapper.WebGameMapper;
import org.example.web.mapper.WebUserMapper;
import org.example.web.model.WebGame;
import org.example.web.model.WebGameField;
import org.example.web.model.WebPlayerWinRatio;
import org.example.web.model.WebUser;
import org.example.web.model.request.CreateGameRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GameControllerTestMock {
    private static final int[][] MATRIX = {
            {1, 0, 0},
            {0, 0, 0},
            {0, 0, 0}
    };

    @Mock
    private GameService gameService;

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    private GameController gameController;

    private UUID currentUserUuid;

    @BeforeEach
    void setUp() {
        gameController = new GameController(gameService, userService, authService,
                new WebGameMapper(), new WebUserMapper());
        currentUserUuid = UUID.randomUUID();
    }

    @Test
    void createGame_passesCurrentUserAndVsComputerTrue() {
        loginAsCurrentUser();
        Game created = game(UUID.randomUUID());
        when(gameService.createGame(currentUserUuid, true)).thenReturn(created);

        ResponseEntity<WebGame> response = gameController.createGame(createRequest(true));

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(created.getUuid(), response.getBody().getUuid());
        verify(gameService).createGame(currentUserUuid, true);
    }

    @Test
    void createGame_passesCurrentUserAndVsComputerFalse() {
        loginAsCurrentUser();
        Game created = game(UUID.randomUUID());
        when(gameService.createGame(currentUserUuid, false)).thenReturn(created);

        ResponseEntity<WebGame> response = gameController.createGame(createRequest(false));

        assertEquals(200, response.getStatusCode().value());
        assertEquals(created.getUuid(), response.getBody().getUuid());
        verify(gameService).createGame(currentUserUuid, false);
    }

    @Test
    void getAllAvailableGames_returnsGamesInSameOrder() {
        Game first = game(UUID.randomUUID());
        Game second = game(UUID.randomUUID());
        when(gameService.getAvailableGames()).thenReturn(List.of(first, second));

        ResponseEntity<List<WebGame>> response = gameController.getAllAvailableGames();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertEquals(first.getUuid(), response.getBody().get(0).getUuid());
        assertEquals(second.getUuid(), response.getBody().get(1).getUuid());
    }

    @Test
    void getAllAvailableGames_returnsEmptyList_whenNoGames() {
        when(gameService.getAvailableGames()).thenReturn(List.of());

        ResponseEntity<List<WebGame>> response = gameController.getAllAvailableGames();

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void joinGame_passesPathUuidAndCurrentUser() {
        loginAsCurrentUser();
        UUID gameUuid = UUID.randomUUID();
        when(gameService.joinGame(gameUuid, currentUserUuid)).thenReturn(game(gameUuid));

        ResponseEntity<WebGame> response = gameController.joinGame(gameUuid);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(gameUuid, response.getBody().getUuid());
        verify(gameService).joinGame(gameUuid, currentUserUuid);
    }

    @Test
    void joinGame_throwsInvalidMove_whenServiceThrows() {
        loginAsCurrentUser();
        UUID gameUuid = UUID.randomUUID();
        when(gameService.joinGame(gameUuid, currentUserUuid))
                .thenThrow(new InvalidMoveException("Cannot join your own game"));

        assertThrows(InvalidMoveException.class, () -> gameController.joinGame(gameUuid));
    }

    @Test
    void getGame_returnsGameWithSameUuid() {
        UUID gameUuid = UUID.randomUUID();
        when(gameService.getGame(gameUuid)).thenReturn(game(gameUuid));

        ResponseEntity<WebGame> response = gameController.getGame(gameUuid);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(gameUuid, response.getBody().getUuid());
    }

    @Test
    void getUser_returnsUser_whenFound() {
        UUID uuid = UUID.randomUUID();
        when(userService.findByUuid(uuid)).thenReturn(Optional.of(user(uuid, "user")));

        ResponseEntity<WebUser> response = gameController.getUser(uuid);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(uuid, response.getBody().getUuid());
        assertEquals("user", response.getBody().getLogin());
    }

    @Test
    void getUser_returnsNotFound_whenMissing() {
        UUID uuid = UUID.randomUUID();
        when(userService.findByUuid(uuid)).thenReturn(Optional.empty());

        ResponseEntity<WebUser> response = gameController.getUser(uuid);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getInfoUser_returnsCurrentUser() {
        loginAsCurrentUser();
        when(userService.findByUuid(currentUserUuid)).thenReturn(Optional.of(user(currentUserUuid, "me")));

        ResponseEntity<WebUser> response = gameController.getInfoUser();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(currentUserUuid, response.getBody().getUuid());
        assertEquals("me", response.getBody().getLogin());
    }

    @Test
    void getInfoUser_returnsNotFound_whenCurrentUserMissing() {
        loginAsCurrentUser();
        when(userService.findByUuid(currentUserUuid)).thenReturn(Optional.empty());

        ResponseEntity<WebUser> response = gameController.getInfoUser();

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void makeMove_passesPathUuidAndFieldFromBody() {
        loginAsCurrentUser();
        UUID pathUuid = UUID.randomUUID();
        WebGame body = new WebGame(UUID.randomUUID(), new WebGameField(MATRIX), null,
                null, null, null, null, 1, -1, null);
        Game returned = game(pathUuid);
        when(gameService.processMove(any(Game.class), eq(currentUserUuid))).thenReturn(returned);

        ResponseEntity<WebGame> response = gameController.makeMove(pathUuid, body);

        verify(gameService).processMove(
                argThat(game -> pathUuid.equals(game.getUuid())
                        && Arrays.deepEquals(MATRIX, game.getGameField().getMatrix())),
                eq(currentUserUuid));
        assertEquals(200, response.getStatusCode().value());
        assertEquals(pathUuid, response.getBody().getUuid());
        assertArrayEquals(returned.getGameField().getMatrix(), response.getBody().getGameField().getMatrix());
    }

    @Test
    void makeMove_throwsInvalidMove_whenServiceThrows() {
        loginAsCurrentUser();
        UUID pathUuid = UUID.randomUUID();
        WebGame body = new WebGame(pathUuid, new WebGameField(MATRIX), null,
                null, null, null, null, 1, -1, null);
        when(gameService.processMove(any(Game.class), eq(currentUserUuid)))
                .thenThrow(new InvalidMoveException("Not your turn"));

        assertThrows(InvalidMoveException.class, () -> gameController.makeMove(pathUuid, body));
    }

    @Test
    void getFinishedGames_returnsGamesOfCurrentUser() {
        loginAsCurrentUser();
        Game finished = game(UUID.randomUUID());
        when(gameService.getFinishedGames(currentUserUuid)).thenReturn(List.of(finished));

        ResponseEntity<List<WebGame>> response = gameController.getFinishedGames();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(finished.getUuid(), response.getBody().get(0).getUuid());
    }

    @Test
    void getLeaderboard_returnsBadRequest_whenNIsNotPositive() {
        ResponseEntity<List<WebPlayerWinRatio>> response = gameController.getLeaderboard(0);

        assertEquals(400, response.getStatusCode().value());
        verifyNoInteractions(gameService, userService);
    }

    @Test
    void getLeaderboard_mapsPlayersWithLogins() {
        UUID knownUuid = UUID.randomUUID();
        UUID unknownUuid = UUID.randomUUID();
        when(gameService.getTopPlayers(10)).thenReturn(List.of(
                new PlayerWinRatio(knownUuid, 2.0),
                new PlayerWinRatio(unknownUuid, 0.5)
        ));
        when(userService.findByUuid(knownUuid)).thenReturn(Optional.of(user(knownUuid, "user")));
        when(userService.findByUuid(unknownUuid)).thenReturn(Optional.empty());

        ResponseEntity<List<WebPlayerWinRatio>> response = gameController.getLeaderboard(10);

        List<WebPlayerWinRatio> body = response.getBody();
        assertEquals(200, response.getStatusCode().value());
        assertAll(
                () -> assertEquals(2, body.size()),
                () -> assertEquals(knownUuid, body.get(0).getUuid()),
                () -> assertEquals("user", body.get(0).getLogin()),
                () -> assertEquals(2.0, body.get(0).getWinRatio()),
                () -> assertEquals(unknownUuid, body.get(1).getUuid()),
                () -> assertNull(body.get(1).getLogin()),
                () -> assertEquals(0.5, body.get(1).getWinRatio())
        );
    }

    private void loginAsCurrentUser() {
        when(authService.getJwtAuthentication())
                .thenReturn(new JwtAuthentication(List.of(Role.USER), currentUserUuid, true));
    }

    private CreateGameRequest createRequest(boolean vsComputer) {
        CreateGameRequest request = new CreateGameRequest();
        request.setVsComputer(vsComputer);
        return request;
    }

    private Game game(UUID uuid) {
        return new Game(uuid, new GameField(), null, GameStatus.WAITING_FOR_PLAYERS,
                null, null, currentUserUuid, null, null);
    }

    private User user(UUID uuid, String login) {
        return new User(uuid, login, "hash", List.of(Role.USER));
    }
}
