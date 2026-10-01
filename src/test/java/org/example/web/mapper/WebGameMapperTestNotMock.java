package org.example.web.mapper;

// =====================================================================
// ВЕРСИЯ ТЕСТА: NOT MOCK (чистый JUnit, без Mockito)
// Тестируем: org.example.web.mapper.WebGameMapper
//
// ПЛАН ТЕСТОВ:
//
// --- toDomain(WebGame) ---
//  - uuid, матрица, status, currentTurn, winner, firstPlayer, secondPlayer перенеслись
//  - computerMark ВСЕГДА null (даже если что-то пришло снаружи), проверь это явно
//
// --- toWeb(Game) ---
//  - поля перенеслись (uuid, матрица, status, currentTurn, winner, players)
//  - метки игроков в WebGame: у первого игрока 1, у второго -1
//    (посмотри геттеры в WebGame, названия не угадывай)
//  - игра без второго игрока (secondPlayerUuid == null) не роняет маппер
// =====================================================================

public class WebGameMapperTestNotMock {
}
