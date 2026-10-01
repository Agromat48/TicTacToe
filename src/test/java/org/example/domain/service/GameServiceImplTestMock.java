package org.example.domain.service;

// =====================================================================
// Заготовка класса (сам допиши аннотации и поля):
//  - @ExtendWith(MockitoExtension.class) над классом
//  - @Mock GameRepository gameRepository
//  - DataGameMapper: НЕ мокай, это простой класс без логики, бери настоящий.
//    ЛОВУШКА: если использовать @InjectMocks, а DataGameMapper не помечен
//    @Mock/@Spy, Mockito передаст в конструктор null. Варианты: @Spy с new
//    DataGameMapper() или создать сервис руками в @BeforeEach, как в NotMock-классе.
//  - Mockito по умолчанию СТРОГИЙ: если настроил when(...), но код его не
//    вызвал, будет UnnecessaryStubbingException. Это не баг, а подсказка,
//    что тест настраивает лишнее.
//  - Данные для мока: DataGame собирается конструктором (uuid, DataGameField,
//    computerMark, status, currentTurnUuid, winnerUuid, firstPlayer, secondPlayer).
//    Сделай хелпер, как gameWith(), только для DataGame.
// =====================================================================

// =====================================================================
// ПЛАН ТЕСТОВ (по одному на ветку, как ты уже делаешь)
// =====================================================================

// --- createGame(creatorUuid, vsComputer) ---
//  - vsComputer = true: вернулась игра с computerMark -1, PLAYER_TURN,
//    currentTurn = creator, firstPlayer = creator, second = null, поле пустое
//  - vsComputer = false: computerMark null, WAITING_FOR_PLAYERS, currentTurn null
//  - в обоих случаях save вызван РОВНО один раз; через ArgumentCaptor проверь,
//    что в репозиторий ушло то же, что вернул метод (uuid, статус)

// --- getAvailableGames() ---
//  - репозиторий вернул 2 игры -> в результате 2 игры
//  - проверь, что findAllByStatus вызван именно со статусом WAITING_FOR_PLAYERS
//    (verify с конкретным аргументом, а не any())
//  - репозиторий вернул пустой список -> пустой список (не null)

// --- getGame(uuid) ---
//  - findById вернул игру -> вернулась игра с тем же uuid
//  - findById вернул Optional.empty() -> InvalidMoveException (assertThrows),
//    проверь и текст сообщения

// --- joinGame(gameUuid, joiningUserUuid) ---
//  - игра не найдена -> InvalidMoveException, save НЕ вызывался (verify never)
//  - статус игры не WAITING_FOR_PLAYERS -> исключение, save не вызывался
//  - игрок присоединяется к своей игре (id совпадает с firstPlayer) -> исключение
//  - успех: статус PLAYER_TURN, currentTurn = firstPlayer, second = joiningUser,
//    поле и первый игрок не изменились, save вызван один раз
//  Подсказка: порядок проверок в методе важен. Придумай случай, который
//  различает "не тот статус" и "своя игра".

import org.example.datasource.mapper.DataGameMapper;
import org.example.datasource.model.DataGame;
import org.example.datasource.model.DataGameField;
import org.example.datasource.repository.GameRepository;
import org.example.domain.exception.InvalidMoveException;
import org.example.domain.model.Game;
import org.example.domain.model.GameStatus;
import org.example.domain.service.GameServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// --- processMove(incomingGame, currentUserUuid) ---  самый большой метод
//  Ветки, которые бросают исключение (везде проверь, что save не вызывался):
//  - игра не найдена
//  - игра уже закончена (в БД поле с выигрышной линией)
//  - сейчас не очередь этого игрока (currentTurn другой)
//  - validation не прошла (изменено больше одной клетки или перезаписана старая)
//  - неправильная метка: первый игрок поставил -1 или второй поставил 1
//  Против компьютера (secondPlayer == null):
//  - обычный ход: бот ответил, на поле стало на 2 метки больше, чем было;
//    статус PLAYER_TURN, currentTurn = игрок
//  - ход игрока выиграл партию: бот НЕ ходит, статус WIN, winnerUuid = игрок,
//    currentTurn = null
//  - ход игрока заполнил поле без победителя -> DRAW
//  Два человека:
//  - обычный ход первого: currentTurn переходит ко второму; и наоборот
//  - победа первого (метка 1) -> WIN, winnerUuid = firstPlayer
//  - победа второго (метка -1) -> WIN, winnerUuid = secondPlayer
//  - ничья -> DRAW
//  - в каждом успешном случае save вызван один раз и сохранён тот же Game,
//    что вернул метод
@ExtendWith(MockitoExtension.class)
public class GameServiceImplTestMock {
    @Mock
    private GameRepository repository;

    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(repository, new DataGameMapper());
    }

    // Пример 1: метод сам создаёт игру. when() не нужен, save только проверяем через verify.
    @Test
    void createGame_returnsPlayerTurnGame_whenVsComputer() {
        UUID creator = UUID.randomUUID();

        Game game = service.createGame(creator, true);

        assertEquals(-1, game.getComputerMark());
        assertEquals(GameStatus.PLAYER_TURN, game.getStatus());
        assertEquals(creator, game.getCurrentTurnUuid());
        assertEquals(creator, game.getFirstPlayerUuid());
        assertNull(game.getSecondPlayerUuid());

        ArgumentCaptor<DataGame> captor = ArgumentCaptor.forClass(DataGame.class);
        verify(repository).save(captor.capture());
        assertEquals(game.getUuid(), captor.getValue().getUuid());
    }

    // Пример 2: код ЧИТАЕТ результат findById, значит его нужно "научить" через when().
    @Test
    void getGame_returnsGame_whenGameExists() {
        UUID gameId = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(waitingGame(gameId, UUID.randomUUID())));

        Game game = service.getGame(gameId);

        Assertions.assertEquals(gameId, game.getUuid());
    }

    // Пример 3: ветка с исключением. Пустой Optional это ответ "в базе ничего нет".
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
    void joinGame_throwsInvalidMove_whenJoiningStartedGame() {
        UUID gameId = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();
        Mockito.when(repository.findById(gameId))
                .thenReturn(Optional.of(twoPlayers(gameId, player1, player2)));

        InvalidMoveException invalidMoveException = assertThrows(InvalidMoveException.class,
                () -> service.joinGame(gameId, UUID.randomUUID()));
        Assertions.assertEquals("Game is not waiting for a second player", invalidMoveException.getMessage());
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

    // Пример 4: при ошибке в базу ничего не должно записаться (verify never).
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
