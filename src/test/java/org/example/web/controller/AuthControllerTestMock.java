package org.example.web.controller;

// =====================================================================
// ВЕРСИЯ ТЕСТА: MOCK (Mockito)
// Тестируем: org.example.web.controller.AuthController
//
// Что мокаем: AuthService (@Mock)
// Без Spring и MockMvc: создаёшь контроллер через new AuthController(authService)
// и вызываешь методы напрямую. Это обычный unit-тест.
//
// ПЛАН ТЕСТОВ:
//
// --- registration(SignUpRequest) ---
//  - authService.registration вернул true  -> ответ со статусом 200
//  - authService.registration вернул false -> ответ со статусом 400
//    (статус: response.getStatusCode().value() или .is2xxSuccessful() / .is4xxClientError())
//  - тело ответа в обоих случаях null
//  - verify: registration вызван ровно один раз с ТЕМ ЖЕ объектом запроса
//
// --- authorization(header) ---
//  - authService вернул uuid -> контроллер вернул тот же uuid
//  - verify: authorization вызван с тем же header
//  - authService бросил InvalidCredentialsException -> исключение
//    пролетает наружу из контроллера (его ловит GameExceptionHandler)
// =====================================================================

public class AuthControllerTestMock {
}
