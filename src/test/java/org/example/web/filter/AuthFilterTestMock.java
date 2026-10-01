package org.example.web.filter;

// =====================================================================
// ВЕРСИЯ ТЕСТА: MOCK (Mockito)
// Тестируем: org.example.web.filter.AuthFilter
//
// Что мокаем: AuthService, HttpServletRequest, HttpServletResponse, FilterChain
//   (все @Mock; фильтр создаёшь как new AuthFilter(authService))
//   Запрос настраивай через when(request.getRequestURI()).thenReturn("/game")
//   и when(request.getHeader("Authorization")).thenReturn("Basic ...").
//
// @AfterEach: SecurityContextHolder.clearContext() (фильтр пишет в статический контекст).
//
// ПЛАН ТЕСТОВ:
//
// --- Публичные пути ---
//  - "/auth/sign_up" -> chain.doFilter вызван, authService НЕ вызывался
//  - "/auth/login"   -> то же самое
//  - в обоих случаях в SecurityContextHolder аутентификации нет
//
// --- Защищённый путь ("/game") ---
//  - успех: authService.authorization вернул uuid -> chain.doFilter вызван один раз,
//    в SecurityContextHolder лежит аутентификация, getPrincipal() == этому uuid
//  - authService бросил InvalidCredentialsException -> response.setStatus(401),
//    chain.doFilter НЕ вызывался (verify never)
//  - заголовка Authorization нет (getHeader вернул null): мок authService
//    сам решит, что вернуть, так что настрой его на исключение,
//    как это делает настоящий AuthService для null
//
// --- Важно ---
//  - Похожий путь "/auth/login/extra" или "/auth/loginx" публичным НЕ считается:
//    фильтр сравнивает через equals. Тест на это (chain вызван, но authService тоже вызван).
//  - В catch стоит NullPointerException: придумай, когда он вылетит. Например,
//    authService вернул null? Разберись, действительно ли так, и напиши тест.
// =====================================================================

public class AuthFilterTestMock {
}
