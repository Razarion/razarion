package com.btxtech.shared.utils;

import com.btxtech.shared.datatypes.Vertex;

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
     * @param angle input
     * @return an angle between 0 and 2 * PI (inclusive)
     */
    static public double normaliseAngle(double angle) {
        if (angle >= ONE_RADIANT) {
            return angle - ONE_RADIANT;
        } else if (angle < 0) {
            return angle + ONE_RADIANT;
        } else {
            return angle;
        }
    }

    /**
     * @param angle input
     * @return if the angle is bigger then PI make it negative
     */
    static public double negateAngle(double angle) {
        angle = normaliseAngle(angle);
        if (angle > Math.PI) {
            angle = angle - ONE_RADIANT;
        }
        return angle;
    }

    public static double closerToAngle(double origin, double angle1, double angle2) {
        origin = normaliseAngle(origin);
        double originNegated = negateAngle(origin);
        double tmpAngle1 = normaliseAngle(angle1);
        double tmpAngle2 = normaliseAngle(angle2);
        double tmpAngleNegated1 = negateAngle(angle1);
        double tmpAngleNegated2 = negateAngle(angle2);

        double delta1 = Math.min(Math.abs(originNegated - tmpAngleNegated1), Math.abs(origin - tmpAngle1));
        double delta2 = Math.min(Math.abs(originNegated - tmpAngleNegated2), Math.abs(origin - tmpAngle2));
        if (delta1 < delta2) {
            return angle1;
        } else {
            return angle2;
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

    public static boolean isInSection(double angle, double startAngle, double deltaAngle) {
        if (Double.isNaN(angle) || Double.isInfinite(angle)) {
            throw new IllegalArgumentException("angle is invalid: " + angle);
        }
        if (Double.isNaN(startAngle) || Double.isInfinite(startAngle)) {
            throw new IllegalArgumentException("startAngle is invalid: " + startAngle);
        }
        if (Double.isNaN(deltaAngle) || Double.isInfinite(deltaAngle)) {
            throw new IllegalArgumentException("deltaAngle is invalid: " + startAngle);
        }

        try {
            angle = normaliseAngle(angle);
            startAngle = normaliseAngle(startAngle);

            if (deltaAngle >= 0) {
                angle = MathHelper.normaliseAngle(angle - startAngle);
                return (angle >= 0 || Math.abs(angle) <= PRECISION) && (angle <= deltaAngle || Math.abs(angle - deltaAngle) <= PRECISION);
            } else {
                startAngle = startAngle + deltaAngle;
                return isInSection(angle, startAngle, -deltaAngle);
            }
        } catch (Throwable throwable) {
            System.out.println("angle: " + MathHelper.radToGrad(angle) + " startAngle: " + MathHelper.radToGrad(startAngle) + " deltaAngle: " + MathHelper.radToGrad(deltaAngle));
            throw new RuntimeException(throwable);
        }
    }

    /**
     * Returns the angle from addItems to end
     *
     * @param startAngle   addItems angle
     * @param endAngle     end angle
     * @param counterClock if true the counter clockwise
     * @return resulting angle
     */
    public static double getAngle(double startAngle, double endAngle, boolean counterClock) {
        startAngle = normaliseAngle(startAngle);
        endAngle = normaliseAngle(endAngle);
        if (counterClock) {
            return normaliseAngle(endAngle - startAngle);
        } else {
            return normaliseAngle(startAngle - endAngle);
        }
    }

    /**
     * Returns the shortest angle from startAngle to endAngle.
     * The shortest angle is taken regardless if clock ore counter-clock wise
     *
     * @param startAngle addItems angle
     * @param endAngle   end angle
     * @return resulting angle
     */
    public static double getAngle(double startAngle, double endAngle) {
        startAngle = normaliseAngle(startAngle);
        endAngle = normaliseAngle(endAngle);
        return Math.abs(Math.min(getAngle(startAngle, endAngle, true), getAngle(startAngle, endAngle, false)));
    }

    public static boolean isCounterClock(double startAngle, double endAngle) {
        return MathHelper.getAngle(startAngle, endAngle, true) < getAngle(startAngle, endAngle, false);
    }

    public static double getRandomAngle() {
        return ONE_RADIANT * Math.random();
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
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            stringBuilder.append(CHARS[(int) (Math.random() * CHARS.length)]);
        }
        stringBuilder.append(System.currentTimeMillis());
        return stringBuilder.toString();
    }

    public static int generateSimpleUuid() {
        return (int) System.currentTimeMillis() + (int) (Math.random() * Integer.MAX_VALUE);
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

    public static double clamp01(double value) {
        return clamp(value, 0.0, 1.0);
    }

    public static double clamp(double value, double edgeMin, double edgeMax, double returnMin, double returnMax) {
        if (value < edgeMin) {
            return returnMin;
        } else if (value > edgeMax) {
            return returnMax;
        } else {
            return value;
        }
    }

    public static double smoothstep(double edge0, double edge1, double value) {
        double t = clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    public static Long getSafeMin(Long l1, Long l2) {
        if (l1 != null && l2 != null) {
            return Math.min(l1, l2);
        } else if (l1 != null) {
            return l1;
        } else if (l2 != null) {
            return l2;
        } else {
            return null;
        }
    }

    public static Long getSafeMax(Long l1, Long l2) {
        if (l1 != null && l2 != null) {
            return Math.max(l1, l2);
        } else if (l1 != null) {
            return l1;
        } else if (l2 != null) {
            return l2;
        } else {
            return null;
        }
    }

    public static double random(Double base, Double variable) {
        double newBase = 0;
        if (base != null) {
            newBase = base;
        }
        if (variable == null || variable == 0) {
            return newBase;
        }

        return newBase - variable + Math.random() * 2.0 * variable;
    }


    public static int random(Integer base, Integer variable) {
        int newBase = 0;
        if (base != null) {
            newBase = base;
        }
        if (variable == null || variable == 0) {
            return newBase;
        }

        return (int) (newBase - variable + Math.random() * 2.0 * variable);
    }

    public static Vertex random(Vertex base, Vertex variable) {
        if (base == null && variable == null) {
            return null;
        }
        if (variable == null) {
            return base;
        }
        Vertex newBase = base;
        if (newBase == null) {
            newBase = Vertex.ZERO;
        }
        return new Vertex(MathHelper.random(newBase.getX(), variable.getX()),
                MathHelper.random(newBase.getY(), variable.getY()),
                MathHelper.random(newBase.getZ(), variable.getZ()));
    }
}
