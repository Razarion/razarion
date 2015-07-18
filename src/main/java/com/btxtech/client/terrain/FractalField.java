package com.btxtech.client.terrain;

import com.btxtech.game.jsre.common.MathHelper;

import java.util.Random;

/**
 * Created by Beat
 * 19.05.2015.
 * <p/>
 * DiamondSquareAlgorithm algorithm
 */
public class FractalField {
    private double[][] terrain;
    private int divisions;
    private Random rng;

    public FractalField(int verticesPerEdge, double roughness) {
        if (!MathHelper.isPowerOfTwo(verticesPerEdge - 1)) {
            throw new IllegalArgumentException("verticesPerEdge must be a power of two number + 1. Given value: " + verticesPerEdge);
        }

        divisions = verticesPerEdge - 1;
        terrain = new double[verticesPerEdge][verticesPerEdge];
        rng = new Random();

        terrain[0][0] = rnd();
        terrain[0][divisions] = rnd();
        terrain[divisions][0] = rnd();
        terrain[divisions][divisions] = rnd();

        double rough = roughness;

        int log2 = (int) MathHelper.log2(divisions);

        for (int i = 0; i < log2; ++i) {
            int r = 1 << (log2 - i);
            int s = r >> 1;
            for (int x = 0; x < divisions; x += r) {
                for (int y = 0; y < divisions; y += r) {
                    diamond(x, y, r, rough);
                }
            }
            if (s > 0) {
                for (int j = 0; j <= divisions; j += s) {
                    for (int k = (j + s) % r; k <= divisions; k += r) {
                        square(j - s, k - s, r, rough);
                    }
                }
            }
            rough *= roughness;
        }
    }

    private void diamond(int x, int y, int side, double scale) {
        if (side > 1) {
            int half = side / 2;
            double avg = (terrain[x][y] + terrain[x + side][y] + terrain[x + side][y + side] + terrain[x][y + side]) / 4.0;
            terrain[x + half][y + half] = avg + rnd() * scale;
        }
    }

    private void square(int x, int y, int side, double scale) {
        int half = side / 2;
        double avg = 0.0;
        double sum = 0.0;
        if (x >= 0) {
            avg += terrain[x][y + half];
            sum += 1.0;
        }
        if (y >= 0) {
            avg += terrain[x + half][y];
            sum += 1.0;
        }
        if (x + side <= divisions) {
            avg += terrain[x + side][y + half];
            sum += 1.0;
        }
        if (y + side <= divisions) {
            avg += terrain[x + half][y + side];
            sum += 1.0;
        }
        terrain[x + half][y + half] = avg / sum + rnd() * scale;
    }

    private double rnd() {
        return 60.0 * rng.nextDouble() - 30.0;
    }

    public double get(int x, int y) {
        return terrain[x][y];
    }

    public int addOffset(int x, int y, double zBase) {
        return (int) (terrain[x][y] * zBase);
    }

    public int getDivisions() {
        return divisions;
    }

    public void setToNull() {
        for (int x = 0; x < terrain.length; x++) {
            for (int y = 0; y < terrain[x].length; y++) {
                terrain[x][y] = 0;
            }
        }
    }

    public static int nearestPossibleNumber(int number1, int number2) {
        return MathHelper.nearestPowerOf2Number(Math.max(number1, number2)) + 1;
    }
}
