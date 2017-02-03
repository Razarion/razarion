package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

import java.util.Arrays;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class Matrix4 {
    public static final int ROWS = 4;
    public static final int COLUMNS = 4;
    public static final Matrix4 IDENTITY = createIdentity();
    public static final Matrix4 ROT_90_Z = createZRotation(MathHelper.QUARTER_RADIANT);
    public static final Matrix4 ROT_180_Z = createZRotation(MathHelper.HALF_RADIANT);
    public static final Matrix4 ROT_270_Z = createZRotation(MathHelper.THREE_QUARTER_RADIANT);
    private double[][] numbers;

    /**
     * Used by Errai
     */
    public Matrix4() {
    }

    public Matrix4(double[] numbers) {
        this.numbers = array2Field(numbers);
    }

    public Matrix4(Matrix4 matrix4) {
        this.numbers = matrix4.elementsCopy();
    }

    private Matrix4(double[][] numbers) {
        this.numbers = numbers;
    }

    public double numberAt(int column, int row) {
        return numbers[row][column];
    }

    public double[][] elementsCopy() {
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
        return numberAt(0, row) * other.numberAt(otherColumn, 0)
                + numberAt(1, row) * other.numberAt(otherColumn, 1)
                + numberAt(2, row) * other.numberAt(otherColumn, 2)
                + numberAt(3, row) * other.numberAt(otherColumn, 3);
    }

    public Vertex multiply(Vertex vertex, double w) {
        return new Vertex(multiply(0, vertex, w), multiply(1, vertex, w), multiply(2, vertex, w));
    }

    public double multiplyW(Vertex vertex, double w) {
        return multiply(3, vertex, w);
    }

    private double multiply(int row, Vertex vertex, double w) {
        return numberAt(0, row) * vertex.getX() + numberAt(1, row) * vertex.getY() + numberAt(2, row) * vertex.getZ() + numberAt(3, row) * w;
    }

    public Matrix4 invertOrNull() {
        double[][] r = new double[ROWS][COLUMNS];

        double r0 = numbers[1][1] * numbers[2][2] * numbers[3][3] - numbers[1][1] * numbers[3][2] * numbers[2][3] - numbers[1][2] * numbers[2][1] * numbers[3][3] + numbers[1][2] * numbers[3][1] * numbers[2][3] + numbers[1][3] * numbers[2][1] * numbers[3][2] - numbers[1][3] * numbers[3][1] * numbers[2][2];
        double r4 = -numbers[1][0] * numbers[2][2] * numbers[3][3] + numbers[1][0] * numbers[3][2] * numbers[2][3] + numbers[1][2] * numbers[2][0] * numbers[3][3] - numbers[1][2] * numbers[3][0] * numbers[2][3] - numbers[1][3] * numbers[2][0] * numbers[3][2] + numbers[1][3] * numbers[3][0] * numbers[2][2];
        double r8 = numbers[1][0] * numbers[2][1] * numbers[3][3] - numbers[1][0] * numbers[3][1] * numbers[2][3] - numbers[1][1] * numbers[2][0] * numbers[3][3] + numbers[1][1] * numbers[3][0] * numbers[2][3] + numbers[1][3] * numbers[2][0] * numbers[3][1] - numbers[1][3] * numbers[3][0] * numbers[2][1];
        double r12 = -numbers[1][0] * numbers[2][1] * numbers[3][2] + numbers[1][0] * numbers[3][1] * numbers[2][2] + numbers[1][1] * numbers[2][0] * numbers[3][2] - numbers[1][1] * numbers[3][0] * numbers[2][2] - numbers[1][2] * numbers[2][0] * numbers[3][1] + numbers[1][2] * numbers[3][0] * numbers[2][1];
        double det = numbers[0][0] * r0 + numbers[0][1] * r4 + numbers[0][2] * r8 + numbers[0][3] * r12;
        if (det == 0.0) {
            return null;
        }
        r[0][0] = r0 / det;
        r[0][1] = (-numbers[0][1] * numbers[2][2] * numbers[3][3] + numbers[0][1] * numbers[3][2] * numbers[2][3] + numbers[0][2] * numbers[2][1] * numbers[3][3] - numbers[0][2] * numbers[3][1] * numbers[2][3] - numbers[0][3] * numbers[2][1] * numbers[3][2] + numbers[0][3] * numbers[3][1] * numbers[2][2]) / det;
        r[0][2] = (numbers[0][1] * numbers[1][2] * numbers[3][3] - numbers[0][1] * numbers[3][2] * numbers[1][3] - numbers[0][2] * numbers[1][1] * numbers[3][3] + numbers[0][2] * numbers[3][1] * numbers[1][3] + numbers[0][3] * numbers[1][1] * numbers[3][2] - numbers[0][3] * numbers[3][1] * numbers[1][2]) / det;
        r[0][3] = (-numbers[0][1] * numbers[1][2] * numbers[2][3] + numbers[0][1] * numbers[2][2] * numbers[1][3] + numbers[0][2] * numbers[1][1] * numbers[2][3] - numbers[0][2] * numbers[2][1] * numbers[1][3] - numbers[0][3] * numbers[1][1] * numbers[2][2] + numbers[0][3] * numbers[2][1] * numbers[1][2]) / det;

        r[1][0] = r4 / det;
        r[1][1] = (numbers[0][0] * numbers[2][2] * numbers[3][3] - numbers[0][0] * numbers[3][2] * numbers[2][3] - numbers[0][2] * numbers[2][0] * numbers[3][3] + numbers[0][2] * numbers[3][0] * numbers[2][3] + numbers[0][3] * numbers[2][0] * numbers[3][2] - numbers[0][3] * numbers[3][0] * numbers[2][2]) / det;
        r[1][2] = (-numbers[0][0] * numbers[1][2] * numbers[3][3] + numbers[0][0] * numbers[3][2] * numbers[1][3] + numbers[0][2] * numbers[1][0] * numbers[3][3] - numbers[0][2] * numbers[3][0] * numbers[1][3] - numbers[0][3] * numbers[1][0] * numbers[3][2] + numbers[0][3] * numbers[3][0] * numbers[1][2]) / det;
        r[1][3] = (numbers[0][0] * numbers[1][2] * numbers[2][3] - numbers[0][0] * numbers[2][2] * numbers[1][3] - numbers[0][2] * numbers[1][0] * numbers[2][3] + numbers[0][2] * numbers[2][0] * numbers[1][3] + numbers[0][3] * numbers[1][0] * numbers[2][2] - numbers[0][3] * numbers[2][0] * numbers[1][2]) / det;

        r[2][0] = r8 / det;
        r[2][1] = (-numbers[0][0] * numbers[2][1] * numbers[3][3] + numbers[0][0] * numbers[3][1] * numbers[2][3] + numbers[0][1] * numbers[2][0] * numbers[3][3] - numbers[0][1] * numbers[3][0] * numbers[2][3] - numbers[0][3] * numbers[2][0] * numbers[3][1] + numbers[0][3] * numbers[3][0] * numbers[2][1]) / det;
        r[2][2] = (numbers[0][0] * numbers[1][1] * numbers[3][3] - numbers[0][0] * numbers[3][1] * numbers[1][3] - numbers[0][1] * numbers[1][0] * numbers[3][3] + numbers[0][1] * numbers[3][0] * numbers[1][3] + numbers[0][3] * numbers[1][0] * numbers[3][1] - numbers[0][3] * numbers[3][0] * numbers[1][1]) / det;
        r[2][3] = (-numbers[0][0] * numbers[1][1] * numbers[2][3] + numbers[0][0] * numbers[2][1] * numbers[1][3] + numbers[0][1] * numbers[1][0] * numbers[2][3] - numbers[0][1] * numbers[2][0] * numbers[1][3] - numbers[0][3] * numbers[1][0] * numbers[2][1] + numbers[0][3] * numbers[2][0] * numbers[1][1]) / det;

        r[3][0] = r12 / det;
        r[3][1] = (numbers[0][0] * numbers[2][1] * numbers[3][2] - numbers[0][0] * numbers[3][1] * numbers[2][2] - numbers[0][1] * numbers[2][0] * numbers[3][2] + numbers[0][1] * numbers[3][0] * numbers[2][2] + numbers[0][2] * numbers[2][0] * numbers[3][1] - numbers[0][2] * numbers[3][0] * numbers[2][1]) / det;
        r[3][2] = (-numbers[0][0] * numbers[1][1] * numbers[3][2] + numbers[0][0] * numbers[3][1] * numbers[1][2] + numbers[0][1] * numbers[1][0] * numbers[3][2] - numbers[0][1] * numbers[3][0] * numbers[1][2] - numbers[0][2] * numbers[1][0] * numbers[3][1] + numbers[0][2] * numbers[3][0] * numbers[1][1]) / det;
        r[3][3] = (numbers[0][0] * numbers[1][1] * numbers[2][2] - numbers[0][0] * numbers[2][1] * numbers[1][2] - numbers[0][1] * numbers[1][0] * numbers[2][2] + numbers[0][1] * numbers[2][0] * numbers[1][2] + numbers[0][2] * numbers[1][0] * numbers[2][1] - numbers[0][2] * numbers[2][0] * numbers[1][1]) / det;

        return new Matrix4(r);
    }

    public Matrix4 invert() {
        Matrix4 inverse = invertOrNull();
        if (inverse != null) {
            return inverse;
        } else {
            throw new IllegalArgumentException("det == 0.0 Unable to build inverse matrix");
        }
    }

    public Matrix4 transpose() {
        double[][] r = new double[ROWS][COLUMNS];

        r[0][0] = numbers[0][0];
        r[0][1] = numbers[1][0];
        r[0][2] = numbers[2][0];
        r[0][3] = numbers[3][0];
        r[1][0] = numbers[0][1];
        r[1][1] = numbers[1][1];
        r[1][2] = numbers[2][1];
        r[1][3] = numbers[3][1];
        r[2][0] = numbers[0][2];
        r[2][1] = numbers[1][2];
        r[2][2] = numbers[2][2];
        r[2][3] = numbers[3][2];
        r[3][0] = numbers[0][3];
        r[3][1] = numbers[1][3];
        r[3][2] = numbers[2][3];
        r[3][3] = numbers[3][3];

        return new Matrix4(r);
    }

    public boolean zero() {
        for (int x = 0; x < COLUMNS; x++) {
            for (int y = 0; y < ROWS; y++) {
                if (x != COLUMNS - 1 || y != ROWS - 1) {
                    if (numbers[x][y] != 0.0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Setup a matrix which can be used for norm transformation.
     * Works fine as long as the scale-transformation is not negative
     *
     * @return matrix for norm transformation
     */
    public Matrix4 normTransformation() {
        Matrix4 inverse = invertOrNull();
        if (inverse != null) {
            return invert().transpose();
        } else {
            return this;
        }
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
                {0, cos, -sin, 0},
                {0, sin, cos, 0},
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

    public static Matrix4 createTranslation(Vertex vertex) {
        return createTranslation(vertex.getX(), vertex.getY(), vertex.getZ());
    }

    public static Matrix4 createTranslation(double x, double y, double z) {
        double[][] numbers = {
                {1, 0, 0, x},
                {0, 1, 0, y},
                {0, 0, 1, z},
                {0, 0, 0, 1}};
        return new Matrix4(numbers);
    }

    public static Matrix4 createScale(double scale) {
        return createScale(scale, scale, scale);
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

    public boolean equalsDelta(Matrix4 other, double delta) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }

        for (int column = 0; column < ROWS; column++) {
            for (int row = 0; row < ROWS; row++) {
                if (!MathHelper.compareWithPrecision(numbers[row][column], other.numbers[row][column], delta)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Due to performance reasons
     * Violates the immutable principe
     * @param position translation
     */
    public void setTranslation(Vertex position) {
        numbers[0][3] = position.getX();
        numbers[1][3] = position.getY();
        numbers[2][3] = position.getZ();
    }

    /**
     * Should only be used be the jaxrs marshaller
     *
     * @return number field
     */
    public double[][] getNumbers() {
        return numbers;
    }

    /**
     * Should only be used be the jaxrs marshaller
     *
     * @param numbers number field
     */
    public void setNumbers(double[][] numbers) {
        this.numbers = numbers;
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


    public static Matrix4 fromField(double[][] numbers) {
        return new Matrix4(copyFiled(numbers));
    }

    public static Matrix4 makeBalancedPerspectiveFrustum(double right, double top, double zNear, double zFar) {
        double x = zNear / right;
        double y = zNear / top;
        double a = -(zFar + zNear) / (zFar - zNear);
        double b = -2 * zFar * zNear / (zFar - zNear);

        return new Matrix4(new double[][]{
                {x, 0, 0, 0},
                {0, y, 0, 0},
                {0, 0, a, b},
                {0, 0, -1, 0}});
    }

    /**
     * http://www.songho.ca/opengl/gl_projectionmatrix.html
     */
    public static Matrix4 makeBalancedOrthographicFrustum(double right, double top, double zNear, double zFar) {
        double a = -2.0 / (zFar - zNear);
        double b = -(zFar + zNear) / (zFar - zNear);

        return new Matrix4(new double[][]{
                {1.0 / right, 0, 0, 0},
                {0, 1.0 / top, 0, 0},
                {0, 0, a, b},
                {0, 0, 0, 1}});
    }

    public static Matrix4 makeTextureCoordinateTransformation() {
        return new Matrix4(new double[][]{
                {0.5, 0.0, 0.0, 0.5},
                {0.0, 0.5, 0.0, 0.5},
                {0.0, 0.0, 0.5, 0.5},
                {0.0, 0.0, 0.0, 1.0}});
    }

    /**
     * Converts a field to array. Row-major order
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
     * Converts a field to array. Column-major order
     *
     * @return array: C0R0, C0R1, C0R2 ... C3R3
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
     * Converts a array to a field. Row-major order
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
        System.out.println(matrix4.numberAt(0, 0) + ", " + matrix4.numberAt(1, 0) + ", " + matrix4.numberAt(2, 0) + ", " + matrix4.numberAt(3, 0));
        System.out.println(matrix4.numberAt(0, 1) + ", " + matrix4.numberAt(1, 1) + ", " + matrix4.numberAt(2, 1) + ", " + matrix4.numberAt(3, 1));
        System.out.println(matrix4.numberAt(0, 2) + ", " + matrix4.numberAt(1, 2) + ", " + matrix4.numberAt(2, 2) + ", " + matrix4.numberAt(3, 2));
        System.out.println(matrix4.numberAt(0, 3) + ", " + matrix4.numberAt(1, 3) + ", " + matrix4.numberAt(2, 3) + ", " + matrix4.numberAt(3, 3));
    }
}
