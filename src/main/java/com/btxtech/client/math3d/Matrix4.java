package com.btxtech.client.math3d;

import java.util.Arrays;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class Matrix4 {
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;
    private double[][] numbers;

    public Matrix4(double[][] numbers) {
        this.numbers = copyFiled(numbers);
    }

    public Matrix4(double[] numbers) {
        this.numbers = array2Field(numbers);
    }

    public Matrix4(Matrix4 matrix4) {
        this.numbers = matrix4.getElementsCopy();
    }

    public double getNumber(int column, int row) {
        return numbers[row][column];
    }

    public double[][] getElementsCopy() {
        return copyFiled(numbers);
    }

    public double[] toArray() {
        return field2Array(numbers);
    }

    /**
     * Converts to an array for WebGl in Column-Major Order
     *
     * @return array
     */
    public double[] toWebGlArray() {
        return field2Array2(numbers);
    }

    public Matrix4 multiply(Matrix4 other) {
        return new Matrix4(new double[][]{
                {multiply(0, other, 0), multiply(0, other, 1), multiply(0, other, 2), multiply(0, other, 3)},
                {multiply(1, other, 0), multiply(1, other, 1), multiply(1, other, 2), multiply(1, other, 3)},
                {multiply(2, other, 0), multiply(2, other, 1), multiply(2, other, 2), multiply(2, other, 3)},
                {multiply(3, other, 0), multiply(3, other, 1), multiply(3, other, 2), multiply(3, other, 3)},
        });
    }

    private double multiply(int row, Matrix4 other, int otherColumn) {
        return getNumber(0, row) * other.getNumber(otherColumn, 0)
                + getNumber(1, row) * other.getNumber(otherColumn, 1)
                + getNumber(2, row) * other.getNumber(otherColumn, 2)
                + getNumber(3, row) * other.getNumber(otherColumn, 3);
    }

    public Vertex multiply(Vertex vertex, double w) {
        return new Vertex(multiply(0, vertex, w), multiply(1, vertex, w), multiply(2, vertex, w));
    }

    public double multiplyW(Vertex vertex, double w) {
        return multiply(3, vertex, w);
    }

    private double multiply(int row, Vertex vertex, double w) {
        return getNumber(0, row) * vertex.getX() + getNumber(1, row) * vertex.getY() + getNumber(2, row) * vertex.getZ() + getNumber(3, row) * w;
    }

    public static Matrix4 createIdentity() {
        double[][] numbers = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createXRotation(double radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double[][] numbers = {
                {1, 0, 0, 0},
                {0, cos, sin, 0},
                {0, -sin, cos, 0},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createYRotation(double radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double[][] numbers = {
                {cos, 0, sin, 0},
                {0, 1, 0, 0},
                {-sin, 0, cos, 0},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createZRotation(double radians) {
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);
        double[][] numbers = {
                {cos, -sin, 0, 0},
                {sin, cos, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createTranslation(double x, double y, double z) {
        double[][] numbers = {
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createScale(double x, double y, double z) {
        double[][] numbers = {
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, z, 0},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    @Override
    public String toString() {
        return "Matrix4{["
                + Arrays.toString(numbers[0])
                + ", "
                + Arrays.toString(numbers[1])
                + ", "
                + Arrays.toString(numbers[2])
                + ", "
                + Arrays.toString(numbers[3])
                + "]}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Matrix4 other = (Matrix4) o;

        for (int column = 0; column < ROWS; column++) {
            for (int row = 0; row < ROWS; row++) {
                if (numbers[row][column] != other.numbers[row][column]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int column = 0; column < ROWS; column++) {
            for (int row = 0; row < ROWS; row++) {
                hashCode = (int) (31 * hashCode + numbers[row][column]);
            }
        }
        return hashCode;
    }

    public static double[][] copyFiled(double[][] inputField) {
        if (inputField.length != ROWS) {
            throw new IllegalArgumentException("Invalid row count: " + inputField.length + ". Expected rows: " + ROWS);
        }
        double[][] outputField = new double[ROWS][COLUMNS];
        for (int i = 0; i < inputField.length; i++) {
            if (inputField[i].length != COLUMNS) {
                throw new IllegalArgumentException("Invalid column count: " + inputField[i].length + "at row " + i + ". Expected rows: " + COLUMNS);
            }
            System.arraycopy(inputField[i], 0, outputField[i], 0, inputField[i].length);
        }
        return outputField;
    }

    /**
     * Converts a field to array.
     *
     * @return array: C0R0, C1R0, C2R0 ... C3R3
     */
    public static double[] field2Array(double[][] field) {
        if (field.length != ROWS) {
            throw new IllegalArgumentException("Invalid row count: " + field.length + ". Expected columns: " + ROWS);
        }
        double[] array = new double[Matrix4.COLUMNS * Matrix4.ROWS];
        for (int row = 0; row < ROWS; row++) {
            if (field[row].length != COLUMNS) {
                throw new IllegalArgumentException("Invalid column count: " + field[row].length + " at row " + row + ". Expected columns: " + COLUMNS);
            }
            System.arraycopy(field[row], 0, array, row * COLUMNS, field[row].length);
        }
        return array;
    }

    /**
     * Converts a field to array.
     *
     * @return array: C0R1, C0R2, C0R3 ... C3R3
     */
    public static double[] field2Array2(double[][] field) {
        if (field.length != ROWS) {
            throw new IllegalArgumentException("Invalid row count: " + field.length + ". Expected columns: " + ROWS);
        }
        double[] array = new double[Matrix4.COLUMNS * Matrix4.ROWS];
        int index = 0;
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                if (field[row].length != COLUMNS) {
                    throw new IllegalArgumentException("Invalid column count: " + field[row].length + " at row " + row + ". Expected columns: " + COLUMNS);
                }
                array[index] = field[row][column];
                index++;
            }
        }
        return array;
    }

    /**
     * Converts a array to a field.
     *
     * @param array array: C0R0, C1R0, C2R0 ... C3R
     */
    public static double[][] array2Field(double[] array) {
        if (array.length != ROWS * COLUMNS) {
            throw new IllegalArgumentException("Invalid count: " + array.length + ". Expected count: " + ROWS * COLUMNS);
        }
        double[][] field = new double[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; row++) {
            System.arraycopy(array, row * COLUMNS, field[row], 0, COLUMNS);
        }
        return field;
    }

    public static void printMatrix(Matrix4 matrix4) {
        System.out.println(matrix4.getNumber(0, 0) + ", " + matrix4.getNumber(1, 0) + ", " + matrix4.getNumber(2, 0) + ", " + matrix4.getNumber(3, 0));
        System.out.println(matrix4.getNumber(0, 1) + ", " + matrix4.getNumber(1, 1) + ", " + matrix4.getNumber(2, 1) + ", " + matrix4.getNumber(3, 1));
        System.out.println(matrix4.getNumber(0, 2) + ", " + matrix4.getNumber(1, 2) + ", " + matrix4.getNumber(2, 2) + ", " + matrix4.getNumber(3, 2));
        System.out.println(matrix4.getNumber(0, 3) + ", " + matrix4.getNumber(1, 3) + ", " + matrix4.getNumber(2, 3) + ", " + matrix4.getNumber(3, 3));
    }
}
