package com.btxtech.client.terrain;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * 23.05.2015.
 */
public class FractalFieldTest {

    @Test
    public void simple() {
        FractalField fractalField = new FractalField(9, 0.25);
        Assert.assertEquals(8, fractalField.getDivisions());

        fractalField.get(0, 0);
        fractalField.get(8, 8);

        try {
            fractalField.get(8, 9);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }

        try {
            fractalField.get(9, 8);
            Assert.fail();
        } catch (ArrayIndexOutOfBoundsException e) {
            // expected
        }
    }

    @Test
    public void testFiled() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_NORMAL;

        FractalField fractalField = new FractalField(9, 0.95);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                double value = fractalField.get(x, y);
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
        }
        System.out.println("max = " + max);
        System.out.println("min = " + min);
    }

    @Test
    public void testFiledNormalize() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_NORMAL;

        FractalField fractalField = new FractalField(9, 0.95);
        fractalField.normalize(1, 0);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                double value = fractalField.get(x, y);
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
        }
        Assert.assertFalse(max > 1.0);
        Assert.assertFalse(min < 0.0);
    }

    @Test
    public void testFiledNormalize2() {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_NORMAL;

        FractalField fractalField = new FractalField(9, 0.95);
        fractalField.normalize(20, -30);
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 9; y++) {
                double value = fractalField.get(x, y);
                System.out.println("value: " + value);
                min = Math.min(value, min);
                max = Math.max(value, max);
            }
        }
        Assert.assertFalse(max > 20.0);
        Assert.assertFalse(min < -30.0);
    }

}