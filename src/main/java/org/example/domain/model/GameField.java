package org.example.domain.model;

public class GameField {
    private int[][] matrix;

    public GameField() {
        this.matrix = new int[3][3];
    }

    public GameField(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
