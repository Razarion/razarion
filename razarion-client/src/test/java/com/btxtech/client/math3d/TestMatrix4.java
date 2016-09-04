package com.btxtech.client.math3d;


import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class TestMatrix4 {

    @Test
    public void convertFiled() {
        double[][] field = Matrix4.copyFiled(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertEquals(Matrix4.ROWS, field.length);
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, field[0], 0.001);
        Assert.assertArrayEquals(new double[]{5, 6, 7, 8}, field[1], 0.001);
        Assert.assertArrayEquals(new double[]{9, 10, 11, 12}, field[2], 0.001);
        Assert.assertArrayEquals(new double[]{13, 14, 15, 16}, field[3], 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertFiledFail1() {
        Matrix4.copyFiled(new double[][]{
                {1, 2, 3, 4, 99},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertFiledFail2() {
        Matrix4.copyFiled(new double[][]{
                {1, 2, 3},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertFiledFail3() {
        Matrix4.copyFiled(new double[][]{
                {1, 2, 3, 4},
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertFiledFail4() {
        Matrix4.copyFiled(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}});
    }

    @Test
    public void field2Array() {
        double[] array = Matrix4.field2Array(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, array, 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2ArrayFail1() {
        Matrix4.field2Array(new double[][]{
                {1, 2, 3},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2ArrayFail2() {
        Matrix4.field2Array(new double[][]{
                {1, 2, 3, 4, 5},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2ArrayFail3() {
        Matrix4.field2Array(new double[][]{
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2ArrayFail4() {
        Matrix4.field2Array(new double[][]{
                {1, 2, 3, 4},
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test
    public void field2Array2() {
        double[] array = Matrix4.field2Array2(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertArrayEquals(new double[]{1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16}, array, 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2Array2Fail1() {
        Matrix4.field2Array2(new double[][]{
                {1, 2, 3},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2Array2Fail2() {
        Matrix4.field2Array2(new double[][]{
                {1, 2, 3, 4, 5},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2Array2Fail3() {
        Matrix4.field2Array2(new double[][]{
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test(expected = IllegalArgumentException.class)
    public void field2Array2Fail4() {
        Matrix4.field2Array2(new double[][]{
                {1, 2, 3, 4},
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
    }

    @Test
    public void array2Field() {
        double[][] field = Matrix4.array2Field(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        Assert.assertEquals(Matrix4.ROWS, field.length);
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, field[0], 0.001);
        Assert.assertArrayEquals(new double[]{5, 6, 7, 8}, field[1], 0.001);
        Assert.assertArrayEquals(new double[]{9, 10, 11, 12}, field[2], 0.001);
        Assert.assertArrayEquals(new double[]{13, 14, 15, 16}, field[3], 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void array2FieldFail1() {
        Matrix4.array2Field(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,});
    }

    @Test(expected = IllegalArgumentException.class)
    public void array2FieldFail2() {
        Matrix4.array2Field(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 99});
    }

    @Test
    public void constructor1() {
        Matrix4 matrix4 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        // Row 0
        Assert.assertEquals(1, matrix4.numberAt(0, 0), 0.001);
        Assert.assertEquals(2, matrix4.numberAt(1, 0), 0.001);
        Assert.assertEquals(3, matrix4.numberAt(2, 0), 0.001);
        Assert.assertEquals(4, matrix4.numberAt(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrix4.numberAt(0, 1), 0.001);
        Assert.assertEquals(6, matrix4.numberAt(1, 1), 0.001);
        Assert.assertEquals(7, matrix4.numberAt(2, 1), 0.001);
        Assert.assertEquals(8, matrix4.numberAt(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrix4.numberAt(0, 2), 0.001);
        Assert.assertEquals(10, matrix4.numberAt(1, 2), 0.001);
        Assert.assertEquals(11, matrix4.numberAt(2, 2), 0.001);
        Assert.assertEquals(12, matrix4.numberAt(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrix4.numberAt(0, 3), 0.001);
        Assert.assertEquals(14, matrix4.numberAt(1, 3), 0.001);
        Assert.assertEquals(15, matrix4.numberAt(2, 3), 0.001);
        Assert.assertEquals(16, matrix4.numberAt(3, 3), 0.001);
    }

    @Test
    public void constructor2() {
        Matrix4 matrix4 = new Matrix4(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
        // Row 0
        Assert.assertEquals(1, matrix4.numberAt(0, 0), 0.001);
        Assert.assertEquals(2, matrix4.numberAt(1, 0), 0.001);
        Assert.assertEquals(3, matrix4.numberAt(2, 0), 0.001);
        Assert.assertEquals(4, matrix4.numberAt(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrix4.numberAt(0, 1), 0.001);
        Assert.assertEquals(6, matrix4.numberAt(1, 1), 0.001);
        Assert.assertEquals(7, matrix4.numberAt(2, 1), 0.001);
        Assert.assertEquals(8, matrix4.numberAt(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrix4.numberAt(0, 2), 0.001);
        Assert.assertEquals(10, matrix4.numberAt(1, 2), 0.001);
        Assert.assertEquals(11, matrix4.numberAt(2, 2), 0.001);
        Assert.assertEquals(12, matrix4.numberAt(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrix4.numberAt(0, 3), 0.001);
        Assert.assertEquals(14, matrix4.numberAt(1, 3), 0.001);
        Assert.assertEquals(15, matrix4.numberAt(2, 3), 0.001);
        Assert.assertEquals(16, matrix4.numberAt(3, 3), 0.001);
    }

    @Test
    public void constructor3() {
        Matrix4 matrix4Tmp = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Matrix4 matrix4 = new Matrix4(matrix4Tmp);
        // Row 0
        Assert.assertEquals(1, matrix4.numberAt(0, 0), 0.001);
        Assert.assertEquals(2, matrix4.numberAt(1, 0), 0.001);
        Assert.assertEquals(3, matrix4.numberAt(2, 0), 0.001);
        Assert.assertEquals(4, matrix4.numberAt(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrix4.numberAt(0, 1), 0.001);
        Assert.assertEquals(6, matrix4.numberAt(1, 1), 0.001);
        Assert.assertEquals(7, matrix4.numberAt(2, 1), 0.001);
        Assert.assertEquals(8, matrix4.numberAt(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrix4.numberAt(0, 2), 0.001);
        Assert.assertEquals(10, matrix4.numberAt(1, 2), 0.001);
        Assert.assertEquals(11, matrix4.numberAt(2, 2), 0.001);
        Assert.assertEquals(12, matrix4.numberAt(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrix4.numberAt(0, 3), 0.001);
        Assert.assertEquals(14, matrix4.numberAt(1, 3), 0.001);
        Assert.assertEquals(15, matrix4.numberAt(2, 3), 0.001);
        Assert.assertEquals(16, matrix4.numberAt(3, 3), 0.001);
    }

    @Test
    public void hashCode1() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertEquals(-237111135, matrix.hashCode());
        matrix = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertEquals(-237111135, matrix.hashCode());
    }

    @Test
    public void hashCode2() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {99, 2, 3, 433},
                {0.5, 6, 44, 8},
                {9, 789, 11, 12},
                {44, 14, 15, 222}});
        Assert.assertEquals(1842244628, matrix.hashCode());
        matrix = new Matrix4(new double[][]{
                {99, 2, 3, 433},
                {0.5, 6, 44, 8},
                {9, 789, 11, 12},
                {44, 14, 15, 222}});
        Assert.assertEquals(1842244628, matrix.hashCode());
    }

    @Test
    public void hashCode3() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}});
        Assert.assertEquals(1353309697, matrix.hashCode());
        matrix = new Matrix4(new double[][]{
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 0, 0}});
        Assert.assertEquals(1353309697, matrix.hashCode());
    }

    @Test
    public void equals1() {
        Matrix4 matrix1 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Matrix4 matrix2 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertTrue(matrix1.equals(matrix2));
        Matrix4 matrix3 = new Matrix4(new double[][]{
                {99, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertFalse(matrix1.equals(matrix3));
        Assert.assertFalse(matrix2.equals(matrix3));
    }

    @Test
    public void equals2() {
        Matrix4 matrix1 = new Matrix4(new double[][]{
                {1, 2, 3, 0.0000001},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Matrix4 matrix2 = new Matrix4(new double[][]{
                {1, 2, 3, 0.000001},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertFalse(matrix1.equals(matrix2));
    }

    @Test
    public void createIdentity() {
        Matrix4 matrix4 = Matrix4.createIdentity();
        // Row 0
        Assert.assertEquals(1, matrix4.numberAt(0, 0), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(1, 0), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(2, 0), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(0, matrix4.numberAt(0, 1), 0.001);
        Assert.assertEquals(1, matrix4.numberAt(1, 1), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(2, 1), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(0, matrix4.numberAt(0, 2), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(1, 2), 0.001);
        Assert.assertEquals(1, matrix4.numberAt(2, 2), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(0, matrix4.numberAt(0, 3), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(1, 3), 0.001);
        Assert.assertEquals(0, matrix4.numberAt(2, 3), 0.001);
        Assert.assertEquals(1, matrix4.numberAt(3, 3), 0.001);
    }

    @Test
    public void createXRotation1() {
        Matrix4 actual = Matrix4.createXRotation(Math.toRadians(10));
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 0, 0, 0},
                {0, 0.984807753012208, -0.17364817766693033, 0},
                {0, 0.17364817766693033, 0.984807753012208, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createXRotation2() {
        Matrix4 actual = Matrix4.createXRotation(Math.toRadians(60));
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 0, 0, 0},
                {0, 0.5000000000000001, -0.8660254037844386, 0},
                {0, 0.8660254037844386, 0.5000000000000001, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createYRotation1() {
        Matrix4 actual = Matrix4.createYRotation(Math.toRadians(10));
        Matrix4 expected = new Matrix4(new double[][]{
                {0.984807753012208, 0, 0.17364817766693033, 0},
                {0, 1, 0, 0},
                {-0.17364817766693033, 0, 0.984807753012208, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createYRotation2() {
        Matrix4 actual = Matrix4.createYRotation(Math.toRadians(60));
        Matrix4 expected = new Matrix4(new double[][]{
                {0.5000000000000001, 0, 0.8660254037844386, 0},
                {0, 1, 0, 0},
                {-0.8660254037844386, 0, 0.5000000000000001, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createZRotation1() {
        Matrix4 actual = Matrix4.createZRotation(Math.toRadians(10));
        Matrix4 expected = new Matrix4(new double[][]{
                {0.984807753012208, -0.17364817766693033, 0, 0},
                {0.17364817766693033, 0.984807753012208, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createZRotation2() {
        Matrix4 actual = Matrix4.createZRotation(Math.toRadians(60));
        Matrix4 expected = new Matrix4(new double[][]{
                {0.5000000000000001, -0.8660254037844386, 0, 0},
                {0.8660254037844386, 0.5000000000000001, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createTranslation1() {
        Matrix4 actual = Matrix4.createTranslation(0.5, -3, 10);
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 0, 0, 0.5},
                {0, 1, 0, -3},
                {0, 0, 1, 10},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createTranslation2() {
        Matrix4 actual = Matrix4.createTranslation(0, 0, 1);
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 1},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createScale1() {
        Matrix4 actual = Matrix4.createScale(0.15, -13, 55);
        Matrix4 expected = new Matrix4(new double[][]{
                {0.15, 0, 0, 0},
                {0, -13, 0, 0},
                {0, 0, 55, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void createScale2() {
        Matrix4 actual = Matrix4.createScale(-11, 0.75, 1);
        Matrix4 expected = new Matrix4(new double[][]{
                {-11, 0, 0, 0},
                {0, 0.75, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void getElementsCopy() {
        Matrix4 matrix4 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        double[][] filed = matrix4.elementsCopy();
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4}, filed[0], 0.001);
        Assert.assertArrayEquals(new double[]{5, 6, 7, 8}, filed[1], 0.001);
        Assert.assertArrayEquals(new double[]{9, 10, 11, 12}, filed[2], 0.001);
        Assert.assertArrayEquals(new double[]{13, 14, 15, 16}, filed[3], 0.001);

        filed[0][0] = 99;
        Assert.assertEquals(1, matrix4.numberAt(0, 0), 0.001);
    }

    @Test
    public void toArray() {
        Matrix4 matrix4 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertArrayEquals(new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16}, matrix4.toArray(), 0.001);
    }

    @Test
    public void toWebGlArray() {
        Matrix4 matrix4 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Assert.assertArrayEquals(new double[]{1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12, 16}, matrix4.toWebGlArray(), 0.001);
    }

    @Test
    public void multiplyMatrix() {
        Matrix4 matrix1 = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Matrix4 matrix2 = new Matrix4(new double[][]{
                {-0.5, 456, 324, 0.001},
                {-234, 4, 87, 1},
                {-654, -7, 0.009, -0.5},
                {17, -214, 98, -18}});
        Matrix4 actual = matrix1.multiply(matrix2);
        Matrix4 expected = new Matrix4(new double[][]{
                {-2362.5, -413, 890.027, -71.499},
                {-5848.5, 543, 2926.063, -141.495},
                {-9334.5, 1499, 4962.099, -211.49099999999999},
                {-12820.5, 2455, 6998.135, -281.487}
        });
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multiplyVector() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Vertex vertex = new Vertex(23, -45, 0.1);
        Vertex actual = matrix.multiply(vertex, 1.0);
        Vertex expected = new Vertex(-62.7, -146.3, -229.9);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void multiplyWVector() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Vertex vertex = new Vertex(23, -45, 0.1);
        double actual = matrix.multiplyW(vertex, 1.0);
        Assert.assertEquals(-313.5, actual, 0.001);
    }

    @Test
    public void inverse() {
        Matrix4 matrix4 = new Matrix4(new double[][]{
                {1, 0, 0, 4},
                {0, 1, 0, 8},
                {0, 0, 1, 6},
                {0, 0, 0, 1}});
        Matrix4 inverse = matrix4.invert();
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 0, 0, -4},
                {0, 1, 0, -8},
                {0, 0, 1, -6},
                {0, 0, 0, 1}});
        Assert.assertEquals(expected, inverse);
    }

    @Test
    public void inverse2() {
        // Separate for translation, scale and rotation
        Matrix4 matrix = Matrix4.createTranslation(-10, 0.4, 23);
        Matrix4 inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
        matrix = Matrix4.createXRotation(Math.toRadians(45));
        inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
        matrix = Matrix4.createXRotation(Math.toRadians(60)).multiply(Matrix4.createYRotation(Math.toRadians(120))).multiply(Matrix4.createZRotation(Math.toRadians(180)));
        inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
        matrix = Matrix4.createScale(-5, 12, 0.8);
        inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
        // translation, scale and rotation together
        matrix = Matrix4.createXRotation(Math.toRadians(45)).multiply(Matrix4.createTranslation(-10, 0.4, 23));
        inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
        matrix = Matrix4.createXRotation(Math.toRadians(45)).multiply(Matrix4.createTranslation(-10, 0.4, 23)).multiply(Matrix4.createScale(-5, 12, 0.8));
        inverse = matrix.invert();
        assertMatrix(Matrix4.createIdentity(), matrix.multiply(inverse));
    }

    @Test
    public void transpose() {
        Matrix4 matrix = new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        Matrix4 transpose = matrix.transpose();
        Matrix4 expected = new Matrix4(new double[][]{
                {1, 5, 9, 13},
                {2, 6, 10, 14},
                {3, 7, 11, 15},
                {4, 8, 12, 16}});

        Assert.assertEquals(expected, transpose);
    }

    @Test
    public void normTransformation() {
        Vertex vector1 = new Vertex(0, 0, 1);
        Vertex vector2 = new Vertex(0, 1, 0);
        Vertex vector3 = new Vertex(1, 0, 0);
        // Rotation
        TestVertex.assertVertex(0, -1, 0, Matrix4.createXRotation(Math.toRadians(90)).normTransformation().multiply(vector1, 0));
        // Translation
        TestVertex.assertVertex(0, 0, 1, Matrix4.createTranslation(-1, 4, 16).normTransformation().multiply(vector1, 0));
        // Scale negative scale does not work
        TestVertex.assertVertex(vector1, Matrix4.createScale(0.5, 0.5, 0.5).normTransformation().multiply(vector1, 0).normalize(1.0));
        TestVertex.assertVertex(vector2, Matrix4.createScale(0.5, 0.5, 0.5).normTransformation().multiply(vector2, 0).normalize(1.0));
        TestVertex.assertVertex(vector3, Matrix4.createScale(0.5, 0.5, 0.5).normTransformation().multiply(vector3, 0).normalize(1.0));
        TestVertex.assertVertex(vector3, Matrix4.createScale(13, 0.5, 27).normTransformation().multiply(vector3, 0).normalize(1.0));
        // Scale rotation and translation together
        TestVertex.assertVertex(0, -1, 0, Matrix4.createScale(13, 0.5, 27).multiply(Matrix4.createXRotation(Math.toRadians(90))).multiply(Matrix4.createTranslation(-1, 4, 16)).normTransformation().multiply(vector1, 0).normalize(1.0));
    }

    public static void assertMatrix(Matrix4 expected, Matrix4 received) {
        String message = "expected:<" + expected + "> but was:<" + received + ">";
        Assert.assertEquals(message, expected.numberAt(0, 0), received.numberAt(0, 0), 0.001);
        Assert.assertEquals(message, expected.numberAt(1, 0), received.numberAt(1, 0), 0.001);
        Assert.assertEquals(message, expected.numberAt(2, 0), received.numberAt(2, 0), 0.001);
        Assert.assertEquals(message, expected.numberAt(3, 0), received.numberAt(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(message, expected.numberAt(0, 1), received.numberAt(0, 1), 0.001);
        Assert.assertEquals(message, expected.numberAt(1, 1), received.numberAt(1, 1), 0.001);
        Assert.assertEquals(message, expected.numberAt(2, 1), received.numberAt(2, 1), 0.001);
        Assert.assertEquals(message, expected.numberAt(3, 1), received.numberAt(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(message, expected.numberAt(0, 2), received.numberAt(0, 2), 0.001);
        Assert.assertEquals(message, expected.numberAt(1, 2), received.numberAt(1, 2), 0.001);
        Assert.assertEquals(message, expected.numberAt(2, 2), received.numberAt(2, 2), 0.001);
        Assert.assertEquals(message, expected.numberAt(3, 2), received.numberAt(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(message, expected.numberAt(0, 3), received.numberAt(0, 3), 0.001);
        Assert.assertEquals(message, expected.numberAt(1, 3), received.numberAt(1, 3), 0.001);
        Assert.assertEquals(message, expected.numberAt(2, 3), received.numberAt(2, 3), 0.001);
        Assert.assertEquals(message, expected.numberAt(3, 3), received.numberAt(3, 3), 0.001);
    }
}
