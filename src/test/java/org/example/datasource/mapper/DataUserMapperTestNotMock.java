package org.example.datasource.mapper;

// =====================================================================
// ВЕРСИЯ ТЕСТА: NOT MOCK (чистый JUnit, без Mockito)
// Тестируем: org.example.datasource.mapper.DataUserMapper
//
// ПЛАН ТЕСТОВ:
//
// --- toDomain(DataUser) ---
//  - uuid, login, password перенеслись в User
//
// --- toDataSource(User) ---
//  - uuid, login, password перенеслись в DataUser
//
// --- Круговой тест ---
//  - User -> toDataSource -> toDomain: поля совпадают
//
// Мини-тест простой, это нормально: цель здесь набить руку на структуру
// arrange / act / assert.
// =====================================================================

public class DataUserMapperTestNotMock {

}
