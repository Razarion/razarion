package com.btxtech.shared.utils;

/**
 * User: beat
 * Date: 23.05.2011
 * Time: 01:06:28
 */
public class MathHelper {
    // Constants
    public final static double ONE_RADIANT = 2.0 * Math.PI;
    public final static double HALF_RADIANT = Math.PI;
    public final static double QUARTER_RADIANT = Math.PI / 2.0;
    public final static double THREE_QUARTER_RADIANT = 3.0 * Math.PI / 2.0;
    public final static double EIGHTH_RADIANT = Math.PI / 4.0;
    public final static double SIX_TEENTH_RADIANT = Math.PI / 8.0;
    public final static double NORTH = 0;
    public final static double NORTH_EAST = 1.75 * Math.PI;
    public final static double EAST = 1.5 * Math.PI;
    public final static double SOUTH_EAST = 1.25 * Math.PI;
    public final static double SOUTH = Math.PI;
    public final static double SOUTH_WEST = 0.75 * Math.PI;
    public final static double WEST = 0.5 * Math.PI;
    public final static double NORTH_WEST = 0.25 * Math.PI;
    public final static double SQRT_OF_2 = Math.sqrt(2.0);
    public final static double PRECISION = 0.001;
    public final static double ZERO_DOT_ONE_DEGREE_IN_RAD = gradToRad(0.1);
    private final static char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * @param angel input
     * @return an angel between 0 and 2 * PI (inclusive)
     */
    static public double normaliseAngel(double angel) {
        if (angel >= ONE_RADIANT) {
            return angel - ONE_RADIANT;
        } else if (angel < 0) {
            return angel + ONE_RADIANT;
        } else {
            return angel;
        }
    }

    /**
     * @param angel input
     * @return if the angel is bigger then PI make it negative
     */
    static public double negateAngel(double angel) {
        angel = normaliseAngel(angel);
        if (angel > Math.PI) {
            angel = angel - ONE_RADIANT;
        }
        return angel;
    }

    public static double closerToAngel(double origin, double angel1, double angel2) {
        origin = normaliseAngel(origin);
        double originNegated = negateAngel(origin);
        double tmpAngel1 = normaliseAngel(angel1);
        double tmpAngel2 = normaliseAngel(angel2);
        double tmpAngelNegated1 = negateAngel(angel1);
        double tmpAngelNegated2 = negateAngel(angel2);

        double delta1 = Math.min(Math.abs(originNegated - tmpAngelNegated1), Math.abs(origin - tmpAngel1));
        double delta2 = Math.min(Math.abs(originNegated - tmpAngelNegated2), Math.abs(origin - tmpAngel2));
        if (delta1 < delta2) {
            return angel1;
        } else {
            return angel2;
        }
    }

    public static double getPythagorasC(double a, double b) {
        return Math.sqrt(a * a + b * b);
    }

    public static double getPythagorasA(double c, double b) {
        return Math.sqrt(c * c - b * b);
    }

    public static int getSqrtOfTwo(double length) {
        return (int) Math.round(SQRT_OF_2 * length);
    }

    public static boolean isInSection(double angel, double startAngel, double deltaAngel) {
        if (Double.isNaN(angel) || Double.isInfinite(angel)) {
            throw new IllegalArgumentException("angel is invalid: " + angel);
        }
        if (Double.isNaN(startAngel) || Double.isInfinite(startAngel)) {
            throw new IllegalArgumentException("startAngel is invalid: " + startAngel);
        }
        if (Double.isNaN(deltaAngel) || Double.isInfinite(deltaAngel)) {
            throw new IllegalArgumentException("deltaAngel is invalid: " + startAngel);
        }

        try {
            angel = normaliseAngel(angel);
            startAngel = normaliseAngel(startAngel);

            if (deltaAngel >= 0) {
                angel = MathHelper.normaliseAngel(angel - startAngel);
                return (angel >= 0 || Math.abs(angel) <= PRECISION) && (angel <= deltaAngel || Math.abs(angel - deltaAngel) <= PRECISION);
            } else {
                startAngel = startAngel + deltaAngel;
                return isInSection(angel, startAngel, -deltaAngel);
            }
        } catch (Throwable throwable) {
            System.out.println("angel: " + MathHelper.radToGrad(angel) + " startAngel: " + MathHelper.radToGrad(startAngel) + " deltaAngel: " + MathHelper.radToGrad(deltaAngel));
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Returns the angel from addItems to end
     *
     * @param startAngel   addItems angel
     * @param endAngel     end angel
     * @param counterClock if true the counter clockwise
     * @return resulting angel
     */
    public static double getAngel(double startAngel, double endAngel, boolean counterClock) {
        startAngel = normaliseAngel(startAngel);
        endAngel = normaliseAngel(endAngel);
        if (counterClock) {
            return normaliseAngel(endAngel - startAngel);
        } else {
            return normaliseAngel(startAngel - endAngel);
        }
    }

    /**
     * Returns the shortest angel from startAngel to endAngel.
     * The shortest angel is taken regardless if clock ore counter-clock wise
     *
     * @param startAngel addItems angel
     * @param endAngel   end angel
     * @return resulting angel
     */
    public static double getAngel(double startAngel, double endAngel) {
        startAngel = normaliseAngel(startAngel);
        endAngel = normaliseAngel(endAngel);
        return Math.abs(Math.min(getAngel(startAngel, endAngel, true), getAngel(startAngel, endAngel, false)));
    }

    public static boolean isCounterClock(double startAngel, double endAngel) {
        return getAngel(startAngel, endAngel, true) < getAngel(startAngel, endAngel, false);
    }


    public static double gradToRad(double grad) {
        return grad / 360.0 * ONE_RADIANT;
    }

    public static double radToGrad(double rad) {
        return rad / ONE_RADIANT * 360.0;
    }

    public static double signum(double value) {
        if (value < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    public static boolean compareWithPrecision(double value1, double value2) {
        return compareWithPrecision(value1, value2, PRECISION);
    }

    public static boolean compareWithPrecision(double value1, double value2, double precision) {
        if (precision < 0) {
            throw new IllegalArgumentException("Precision is not allowed to be smaller than 0");
        }
        if (Double.compare(value1, value2) == 0) {
            return true;
        }
        double delta = Math.abs(value1 - value2);
        return delta <= precision;
    }

    public static String generateUuid() {
        char[] uuid = new char[16];
        for (int i = 0; i < uuid.length; i++) {
            uuid[i] = CHARS[(int) (Math.random() * CHARS.length)];
        }
        return new String(uuid);
    }

    /**
     * Randomly get true or false relaying on the given possibility
     *
     * @param possibility 0..1 (0..100%)
     * @return true ore false randomly
     */
    public static boolean isRandomPossibility(double possibility) {
        if (possibility > 1.0) {
            possibility = 1.0;
        } else if (possibility < 0.0) {
            possibility = 0.0;
        }
        return compareWithPrecision(possibility, 1.0) || !MathHelper.compareWithPrecision(possibility, 0.0) && Math.random() <= possibility;
    }

    public static boolean isSquareNumber(int number) {
        int sqrt = (int) Math.sqrt(number);
        return sqrt * sqrt == number;
    }

    public static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    public static double log2(int n) {
        return (Math.log(n) / Math.log(2));
    }

    public static int nearestPowerOf2Number(int number) {
        int log2 = (int) Math.ceil(MathHelper.log2(number));
        return (int) Math.pow(2, log2);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }


    public static double clamp(double value, double edgeMin, double edgeMax, double returnMin, double returnMax) {
        if(value < edgeMin) {
            return returnMin;
        } else if(value > edgeMax) {
            return returnMax;
        } else {
            return value;
        }
    }

    public static double smoothstep(double edge0, double edge1, double value) {
        double t = clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);    }


}
