package org.example.datasource.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tools.jackson.databind.ObjectMapper;

@Converter
public class GameFieldConverter implements AttributeConverter<DataGameField, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(DataGameField attribute) {
        if (attribute == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(attribute.getMatrix());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize game field matrix", e);
        }
    }

    @Override
    public DataGameField convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            int[][] matrix = objectMapper.readValue(dbData, int[][].class);
            return new DataGameField(matrix);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize game field matrix", e);
        }
    }
}