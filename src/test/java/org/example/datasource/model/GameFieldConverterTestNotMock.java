package org.example.datasource.model;

// =====================================================================
// ВЕРСИЯ ТЕСТА: NOT MOCK (чистый JUnit, без Mockito)
// Тестируем: org.example.datasource.model.GameFieldConverter
//
// Конвертер сам создаёт ObjectMapper внутри, так что просто new GameFieldConverter().
//
// ПЛАН ТЕСТОВ:
//
// --- convertToDatabaseColumn(DataGameField) ---
//  - null -> null
//  - пустое поле -> "[[0,0,0],[0,0,0],[0,0,0]]"
//  - поле с крестиками/ноликами -> правильная JSON-строка (отрицательные числа!)
//
// --- convertToEntityAttribute(String) ---
//  - null -> null
//  - валидная JSON-строка -> DataGameField с нужной матрицей (assertArrayEquals)
//  - мусор ("not a json") -> IllegalStateException, проверь текст сообщения
//  - валидный JSON, но не матрица (например "{\"a\":1}") -> что происходит? зафиксируй
//
// --- Круговой тест ---
//  - DataGameField -> строка -> DataGameField: матрица не изменилась
// =====================================================================

public class GameFieldConverterTestNotMock {
}
