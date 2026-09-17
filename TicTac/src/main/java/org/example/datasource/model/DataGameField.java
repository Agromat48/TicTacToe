package org.example.datasource.model;

public class DataGameField {

    private final int[][] matrix;

    public DataGameField() {
        this.matrix = new int[3][3];
    }

    public DataGameField(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
