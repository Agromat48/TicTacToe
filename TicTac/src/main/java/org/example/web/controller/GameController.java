package org.example.web.controller;

import org.example.domain.model.Game;
import org.example.web.mapper.WebGameMapper;
import org.example.domain.service.GameService;
import org.example.domain.service.UserService;
import org.example.web.mapper.WebUserMapper;
import org.example.web.model.CreateGameRequest;
import org.example.web.model.WebGame;
import org.example.web.model.WebUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class GameController {
    private final GameService gameService;
    private final UserService userService;
    private final WebGameMapper webGameMapper;
    private final WebUserMapper webUserMapper;

    public GameController(GameService gameService,
                          UserService userService,
                          WebGameMapper webGameMapper,
                          WebUserMapper webUserMapper
    ) {
        this.gameService = gameService;
        this.userService = userService;
        this.webGameMapper = webGameMapper;
        this.webUserMapper = webUserMapper;
    }

    @PostMapping("/game/{uuid}")
    public ResponseEntity<WebGame> makeMove(
            @PathVariable UUID uuid,
            @RequestBody WebGame webGame
    ) {
        Game mappedGame = webGameMapper.toDomain(webGame);

        Game game = new Game(
                uuid,
                mappedGame.getGameField(),
                null,
                null,
                null,
                null,
                null,
                null
        );

        Game newGame = gameService.processMove(game, getCurrentUserUuid());
        WebGame newWebGame = webGameMapper.toWeb(newGame);
        return ResponseEntity.ok(newWebGame);
    }

    @PostMapping("/game")
    public ResponseEntity<WebGame> createGame(
            @RequestBody CreateGameRequest request
    ) {
        Game game = gameService.createGame(getCurrentUserUuid(), request.isVsComputer());
        return ResponseEntity.ok(webGameMapper.toWeb(game));
    }

    @GetMapping("/game/available")
    public ResponseEntity<List<WebGame>> getAllAvailableGames() {
        return ResponseEntity.ok(gameService
                .getAvailableGames()
                .stream()
                .map(webGameMapper::toWeb)
                .collect(Collectors.toList())
        );
    }

    @PostMapping("/game/{uuid}/join")
    public ResponseEntity<WebGame> joinGame(
            @PathVariable UUID uuid
    ) {
        Game game = gameService.joinGame(uuid, getCurrentUserUuid());
        return ResponseEntity.ok(webGameMapper.toWeb(game));
    }

    @GetMapping("/game/{uuid}")
    public ResponseEntity<WebGame> getGame(
            @PathVariable UUID uuid
    ) {
        Game game = gameService.getGame(uuid);
        return ResponseEntity.ok(webGameMapper.toWeb(game));
    }

    @GetMapping("/user/{uuid}")
    public ResponseEntity<WebUser> getUser(
            @PathVariable UUID uuid
    ) {
        return userService.findByUuid(uuid)
                .map(user -> ResponseEntity.ok(webUserMapper.toWeb(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    private UUID getCurrentUserUuid() {
        return (UUID) Objects.requireNonNull(SecurityContextHolder
                        .getContext()
                        .getAuthentication())
                .getPrincipal();
    }
}