package org.example.domain.service;

// =====================================================================
// ВЕРСИЯ ТЕСТА: MOCK (Mockito)
// Тестируем: org.example.domain.service.AuthService
//
// Что мокаем: UserService (@Mock), PasswordEncoder (@Mock)
//   Настоящий BCrypt не нужен: настрой when(passwordEncoder.encode(...))
//   и when(passwordEncoder.matches(...)).
//
// Хелпер: basicHeader(login, password) ->
//   "Basic " + Base64.getEncoder().encodeToString((login + ":" + password).getBytes())
//
// ПЛАН ТЕСТОВ:
//
// --- registration(SignUpRequest) ---
//  - успех: вернулся true; createUser вызван с логином и ХЕШЕМ пароля
//    (то, что вернул encode), а не с сырым паролем
//  - userService.createUser бросает UserAlreadyExistsException -> вернулся false
//    (настройка мока на исключение: when(...).thenThrow(...))
//
// --- authorization(header) ---
//  - header == null -> InvalidCredentialsException
//  - header не начинается с "Basic " (например "Bearer abc") -> InvalidCredentialsException,
//    userService при этом НЕ вызывался (verify never / verifyNoInteractions)
//  - верный логин и пароль -> вернулся uuid найденного пользователя
//  - пользователь не найден (Optional.empty()) -> InvalidCredentialsException
//  - пользователь найден, но matches вернул false -> InvalidCredentialsException
//
// --- ГРАНИЧНЫЕ СЛУЧАИ (посмотри, что реально происходит, и реши: это норма или баг?) ---
//  - "Basic " + Base64 от строки без двоеточия ("loginonly")
//  - "Basic " + невалидный Base64 ("!!!")
//  - пароль, содержащий двоеточие ("pa:ss")
//  Зафиксируй текущее поведение тестом (assertThrows на то исключение, которое есть).
// =====================================================================

public class AuthServiceTestMock {
}
