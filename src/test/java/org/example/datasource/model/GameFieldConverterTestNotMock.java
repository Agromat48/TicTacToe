package org.example.datasource.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameFieldConverterTestNotMock {

    private static final int[][] MATRIX = {
            {1, -1, 0},
            {0, 1, 0},
            {-1, 0, 1}
    };

    private GameFieldConverter converter;

    @BeforeEach
    void setUp() {
        converter = new GameFieldConverter();
    }

    @Test
    void convertToDatabaseColumn_returnsNull_whenAttributeIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_returnsZeroMatrix_whenFieldIsEmpty() {
        String json = converter.convertToDatabaseColumn(new DataGameField());

        assertEquals("[[0,0,0],[0,0,0],[0,0,0]]", json);
    }

    @Test
    void convertToDatabaseColumn_returnsJson_whenFieldHasMarks() {
        String json = converter.convertToDatabaseColumn(new DataGameField(MATRIX));

        assertEquals("[[1,-1,0],[0,1,0],[-1,0,1]]", json);
    }

    @Test
    void convertToEntityAttribute_returnsNull_whenDbDataIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_returnsField_whenJsonIsValid() {
        DataGameField field = converter.convertToEntityAttribute("[[1,-1,0],[0,1,0],[-1,0,1]]");

        assertArrayEquals(MATRIX, field.getMatrix());
    }

    @Test
    void convertToEntityAttribute_throwsIllegalState_whenNotJson() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> converter.convertToEntityAttribute("not a json"));

        assertEquals("Unable to deserialize game field matrix", exception.getMessage());
    }

    @Test
    void convertToEntityAttribute_throwsIllegalState_whenJsonIsNotMatrix() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> converter.convertToEntityAttribute("{\"a\":1}"));

        assertEquals("Unable to deserialize game field matrix", exception.getMessage());
    }

    @Test
    void roundTrip_fieldToStringToField_keepsMatrix() {
        DataGameField result = converter.convertToEntityAttribute(
                converter.convertToDatabaseColumn(new DataGameField(MATRIX)));

        assertArrayEquals(MATRIX, result.getMatrix());
    }
}
