package com.btxtech.shared.utils;

import com.btxtech.shared.dto.FractalFieldConfig;

/**
 * Created by Beat
 * 23.01.2016.
 * <p/>
 * Diamond-square algorithm
 */
public class FractalFieldGenerator {
    // private final static Logger logger = Logger.getLogger(FractalFieldGenerator.class.getName());
    private final int verticesPerEdge;
    private final double minValue;
    private final double maxValue;
    private final int divisions;
    private final int log2;
    private final double[][] values;
    private final double roughness;

    private FractalFieldGenerator(int verticesPerEdge, double roughness, double minValue, double maxValue) {
        this.verticesPerEdge = verticesPerEdge;
        this.minValue = minValue;
        this.maxValue = maxValue;
        divisions = verticesPerEdge - 1;
        log2 = (int) MathHelper.log2(divisions);
        this.roughness = roughness;
        values = new double[verticesPerEdge][verticesPerEdge];
        clearValues();
        values[0][0] = randomInit();
        values[0][divisions] = randomInit();
        values[divisions][0] = randomInit();
        values[divisions][divisions] = randomInit();
    }

    private void process() {
        double rough = roughness;
        for (int i = 0; i < log2; i++) {
            int side = 1 << (log2 - i);
            for (int x = 0; x < divisions; x += side) {
                for (int y = 0; y < divisions; y += side) {
                    square(x, y, side, rough);
                }
            }
            int s = side >> 1;
            if (s > 0) {
                for (int j = 0; j <= divisions; j += s) {
                    for (int k = (j + s) % side; k <= divisions; k += side) {
                        diamond(j - s, k - s, side, rough);
                    }
                }
                rough *= roughness;
            }
        }
    }

    private double getValue(int x, int y) {
        return values[x][y];
    }

    private void square(int x, int y, int side, double scale) {
        if (side > 1) {
            int half = side / 2;
            double avg = (values[x][y] + values[x + side][y] + values[x][y + side] + values[x + side][y + side]) / 4.0;
            values[x + half][y + half] = clamp(avg + random(scale));
        }
    }

    private void diamond(int x, int y, int side, double scale) {
        int half = side / 2;
        int factor = 0;
        double sum = 0.0;
        if (x >= 0) {
            sum = values[x][y + half];
            factor++;
        }
        if (y >= 0) {
            sum += values[x + half][y];
            factor++;
        }
        if (x + side <= divisions) {
            sum += values[x + side][y + half];
            factor++;
        }
        if (y + side <= divisions) {
            sum += values[x + half][y + side];
            factor++;
        }
        values[x + half][y + half] = clamp(sum / (double) factor + random(scale));
    }

    private double randomInit() {
        double delta = maxValue - minValue;
        return Math.random() * delta + minValue;
    }

    private double random(double scale) {
        double delta = maxValue - minValue;
        return (Math.random() - 0.5) * delta * scale;
    }

    private double clamp(double value) {
        return MathHelper.clamp(value, minValue, maxValue);
    }

    private void clearValues() {
        for (int row = 0; row < verticesPerEdge; row++) {
            for (int column = 0; column < verticesPerEdge; column++) {
                values[column][row] = 0;
            }
        }
    }

    private static int nearestPossibleNumber(int number1, int number2) {
        int maxNumber = Math.max(number1, number2);

        if (MathHelper.isPowerOfTwo(maxNumber - 1)) {
            return maxNumber;
        }

        return MathHelper.nearestPowerOf2Number(maxNumber) + 1;
    }

    private static void clamp(FractalFieldConfig fractalFieldConfig, double[][] clampedFractalField) {
        if (fractalFieldConfig.getClampMax() < 1.0 && fractalFieldConfig.getClampMin() > 0.0) {
            double minEdge = InterpolationUtils.mix(fractalFieldConfig.getFractalMin(), fractalFieldConfig.getFractalMax(), fractalFieldConfig.getClampMin());
            double maxEdge = InterpolationUtils.mix(fractalFieldConfig.getFractalMin(), fractalFieldConfig.getFractalMax(), fractalFieldConfig.getClampMax());
            for (int x = 0; x < fractalFieldConfig.getXCount(); x++) {
                for (int y = 0; y < fractalFieldConfig.getYCount(); y++) {
                    clampedFractalField[x][y] = MathHelper.clamp(clampedFractalField[x][y], minEdge, maxEdge, fractalFieldConfig.getFractalMin(), fractalFieldConfig.getFractalMax());
                }
            }
        }
    }

    public static double[][] createFractalField(FractalFieldConfig fractalFieldConfig) {
        // long time = System.currentTimeMillis();
        FractalFieldGenerator fractalFieldGenerator = new FractalFieldGenerator(nearestPossibleNumber(fractalFieldConfig.getXCount(), fractalFieldConfig.getYCount()), fractalFieldConfig.getFractalRoughness(), fractalFieldConfig.getFractalMin(), fractalFieldConfig.getFractalMax());
        // time = System.currentTimeMillis();
        fractalFieldGenerator.process();
        double[][] values = new double[fractalFieldConfig.getXCount()][fractalFieldConfig.getYCount()];
        for (int x = 0; x < fractalFieldConfig.getXCount(); x++) {
            for (int y = 0; y < fractalFieldConfig.getYCount(); y++) {
                values[x][y] = fractalFieldGenerator.getValue(x, y);
            }
        }

        clamp(fractalFieldConfig, values);
        return values;
    }

    public static double[][] createFlatField(FractalFieldConfig fractalFieldConfig, double value) {
        double[][] values = new double[fractalFieldConfig.getXCount()][fractalFieldConfig.getYCount()];
        for (int x = 0; x < fractalFieldConfig.getXCount(); x++) {
            for (int y = 0; y < fractalFieldConfig.getYCount(); y++) {
                values[x][y] = value;
            }
        }

        return values;
    }
}
