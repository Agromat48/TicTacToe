package org.example.web.exception;

// =====================================================================
// ВЕРСИЯ ТЕСТА: NOT MOCK (чистый JUnit, без Mockito)
// Тестируем: org.example.web.exception.GameExceptionHandler
//
// Хендлер без зависимостей: new GameExceptionHandler(), вызываешь метод
// с созданным исключением и смотришь на ResponseEntity.
//
// ПЛАН ТЕСТОВ:
//
// --- handleInvalidMove(InvalidMoveException) ---
//  - статус 400, в теле ErrorResponse с message = сообщению исключения
//
// --- handleUserAlredyExists(UserAlreadyExistsException) ---
//  - статус 401 (да, именно тот, что в коде сейчас), message совпадает
//  - подумай: 401 для "пользователь уже существует" это осознанное решение?
//    Тест фиксирует текущее поведение, если решишь менять, тест поменяешь тоже.
//
// --- handleInvalidCredentials(InvalidCredentialsException) ---
//  - статус 401, message совпадает
//
// Проверь: тело ответа не null.
// =====================================================================

public class GameExceptionHandlerTestNotMock {
}
