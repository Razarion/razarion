package com.btxtech.shared.datatypes;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class Matrix4Builder {
    private double[][] numbers;

    public Matrix4Builder(double[][] numbers) {
        this.numbers = Matrix4.copyFiled(numbers);
    }

    public Matrix4Builder(Matrix4 matrix4) {
        this.numbers = matrix4.elementsCopy();
    }

    public Matrix4 toMatrix4() {
        return new Matrix4(Matrix4.field2Array(numbers));
    }

    public double getNumber(int column, int row) {
        return numbers[row][column];
    }

    public void setNumber(int column, int row, double number) {
        numbers[row][column] = number;
    }
}
