package org.example.web.model;

public class WebGameField {
    private final int[][] matrix;

    public WebGameField() {
        this.matrix = new int[3][3];
    }

    public WebGameField(int[][] matrix) {
        this.matrix = matrix;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
