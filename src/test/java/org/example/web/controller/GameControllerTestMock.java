package org.example.web.controller;

// =====================================================================
// ВЕРСИЯ ТЕСТА: MOCK (Mockito)
// Тестируем: org.example.web.controller.GameController
//
// Что мокаем:    GameService, UserService (@Mock)
// Что НЕ мокаем: WebGameMapper, WebUserMapper (простые, бери настоящие;
//                собери контроллер руками в @BeforeEach)
//
// Как подставить "текущего пользователя": контроллер берёт uuid из
// SecurityContextHolder. Значит:
//  - @BeforeEach: SecurityContextHolder.getContext().setAuthentication(
//        new UsernamePasswordAuthenticationToken(currentUserUuid, null, List.of()))
//  - @AfterEach:  SecurityContextHolder.clearContext()  (иначе тесты потекут друг в друга)
//
// Без ArgumentCaptor: для проверки аргумента используй verify(...) с конкретными
// значениями или eq(...) / argThat(...) из Mockito.
//
// ПЛАН ТЕСТОВ:
//
// --- createGame(CreateGameRequest) ---
//  - gameService.createGame вызван с currentUserUuid и флагом vsComputer из запроса
//    (проверь оба значения: true и false)
//  - ответ 200, в теле WebGame с uuid созданной игры
//
// --- getAllAvailableGames() ---
//  - сервис вернул 2 игры -> в теле список из 2 WebGame (uuid по порядку)
//  - сервис вернул пустой список -> 200 и пустой список
//
// --- joinGame(uuid) ---
//  - gameService.joinGame вызван с (uuid из пути, currentUserUuid)
//  - ответ 200, в теле WebGame
//  - сервис бросил InvalidMoveException -> исключение пролетает наружу
//
// --- getGame(uuid) ---
//  - ответ 200, в теле игра с тем же uuid
//
// --- getUser(uuid) ---
//  - userService.findByUuid вернул User   -> 200 и WebUser с uuid/login
//  - userService.findByUuid вернул empty  -> 404
//
// --- makeMove(uuid, WebGame) ---
//  - processMove вызван с currentUserUuid, а у переданной в сервис Game
//    uuid = uuid ИЗ ПУТИ (не из тела запроса!), проверь через argThat
//  - поле в переданной игре = поле из тела запроса
//  - ответ 200, в теле то, что вернул сервис (после toWeb)
//  - сервис бросил InvalidMoveException -> пролетает наружу
// =====================================================================

public class GameControllerTestMock {
}
