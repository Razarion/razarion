package com.btxtech.client.math3d;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Matrix4Builder;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 05.04.2015.
 */
public class TestMatrix4Builder {
    @Test
    public void constructor1() {
        Matrix4Builder matrixBuilder = new Matrix4Builder(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        // Row 0
        Assert.assertEquals(1, matrixBuilder.getNumber(0, 0), 0.001);
        Assert.assertEquals(2, matrixBuilder.getNumber(1, 0), 0.001);
        Assert.assertEquals(3, matrixBuilder.getNumber(2, 0), 0.001);
        Assert.assertEquals(4, matrixBuilder.getNumber(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrixBuilder.getNumber(0, 1), 0.001);
        Assert.assertEquals(6, matrixBuilder.getNumber(1, 1), 0.001);
        Assert.assertEquals(7, matrixBuilder.getNumber(2, 1), 0.001);
        Assert.assertEquals(8, matrixBuilder.getNumber(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrixBuilder.getNumber(0, 2), 0.001);
        Assert.assertEquals(10, matrixBuilder.getNumber(1, 2), 0.001);
        Assert.assertEquals(11, matrixBuilder.getNumber(2, 2), 0.001);
        Assert.assertEquals(12, matrixBuilder.getNumber(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrixBuilder.getNumber(0, 3), 0.001);
        Assert.assertEquals(14, matrixBuilder.getNumber(1, 3), 0.001);
        Assert.assertEquals(15, matrixBuilder.getNumber(2, 3), 0.001);
        Assert.assertEquals(16, matrixBuilder.getNumber(3, 3), 0.001);
    }

    @Test
    public void constructor2() {
        Matrix4Builder matrixBuilder = new Matrix4Builder(new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}}));
        // Row 0
        Assert.assertEquals(1, matrixBuilder.getNumber(0, 0), 0.001);
        Assert.assertEquals(2, matrixBuilder.getNumber(1, 0), 0.001);
        Assert.assertEquals(3, matrixBuilder.getNumber(2, 0), 0.001);
        Assert.assertEquals(4, matrixBuilder.getNumber(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrixBuilder.getNumber(0, 1), 0.001);
        Assert.assertEquals(6, matrixBuilder.getNumber(1, 1), 0.001);
        Assert.assertEquals(7, matrixBuilder.getNumber(2, 1), 0.001);
        Assert.assertEquals(8, matrixBuilder.getNumber(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrixBuilder.getNumber(0, 2), 0.001);
        Assert.assertEquals(10, matrixBuilder.getNumber(1, 2), 0.001);
        Assert.assertEquals(11, matrixBuilder.getNumber(2, 2), 0.001);
        Assert.assertEquals(12, matrixBuilder.getNumber(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrixBuilder.getNumber(0, 3), 0.001);
        Assert.assertEquals(14, matrixBuilder.getNumber(1, 3), 0.001);
        Assert.assertEquals(15, matrixBuilder.getNumber(2, 3), 0.001);
        Assert.assertEquals(16, matrixBuilder.getNumber(3, 3), 0.001);
    }

    @Test
    public void toMatrix() {
        Matrix4Builder matrixBuilder = new Matrix4Builder(new Matrix4(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}}));
        Matrix4 matrix4 = matrixBuilder.toMatrix4();
        // Row 0
        Assert.assertEquals(1, matrix4.getNumber(0, 0), 0.001);
        Assert.assertEquals(2, matrix4.getNumber(1, 0), 0.001);
        Assert.assertEquals(3, matrix4.getNumber(2, 0), 0.001);
        Assert.assertEquals(4, matrix4.getNumber(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrix4.getNumber(0, 1), 0.001);
        Assert.assertEquals(6, matrix4.getNumber(1, 1), 0.001);
        Assert.assertEquals(7, matrix4.getNumber(2, 1), 0.001);
        Assert.assertEquals(8, matrix4.getNumber(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrix4.getNumber(0, 2), 0.001);
        Assert.assertEquals(10, matrix4.getNumber(1, 2), 0.001);
        Assert.assertEquals(11, matrix4.getNumber(2, 2), 0.001);
        Assert.assertEquals(12, matrix4.getNumber(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrix4.getNumber(0, 3), 0.001);
        Assert.assertEquals(14, matrix4.getNumber(1, 3), 0.001);
        Assert.assertEquals(15, matrix4.getNumber(2, 3), 0.001);
        Assert.assertEquals(16, matrix4.getNumber(3, 3), 0.001);
    }

    @Test
    public void setNumber() {
        Matrix4Builder matrixBuilder = new Matrix4Builder(new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 16}});
        matrixBuilder.setNumber(0, 0, 1001);
        matrixBuilder.setNumber(3, 0, 1002);
        matrixBuilder.setNumber(2, 3, 1003);
        matrixBuilder.setNumber(2, 1, 1004);


        Matrix4 matrix4 = matrixBuilder.toMatrix4();
        // Row 0
        Assert.assertEquals(1001, matrix4.getNumber(0, 0), 0.001);
        Assert.assertEquals(2, matrix4.getNumber(1, 0), 0.001);
        Assert.assertEquals(3, matrix4.getNumber(2, 0), 0.001);
        Assert.assertEquals(1002, matrix4.getNumber(3, 0), 0.001);
        // Row 1
        Assert.assertEquals(5, matrix4.getNumber(0, 1), 0.001);
        Assert.assertEquals(6, matrix4.getNumber(1, 1), 0.001);
        Assert.assertEquals(1004, matrix4.getNumber(2, 1), 0.001);
        Assert.assertEquals(8, matrix4.getNumber(3, 1), 0.001);
        // Row 2
        Assert.assertEquals(9, matrix4.getNumber(0, 2), 0.001);
        Assert.assertEquals(10, matrix4.getNumber(1, 2), 0.001);
        Assert.assertEquals(11, matrix4.getNumber(2, 2), 0.001);
        Assert.assertEquals(12, matrix4.getNumber(3, 2), 0.001);
        // Row 3
        Assert.assertEquals(13, matrix4.getNumber(0, 3), 0.001);
        Assert.assertEquals(14, matrix4.getNumber(1, 3), 0.001);
        Assert.assertEquals(1003, matrix4.getNumber(2, 3), 0.001);
        Assert.assertEquals(16, matrix4.getNumber(3, 3), 0.001);
    }


}
