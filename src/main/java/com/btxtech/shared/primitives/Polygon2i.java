package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.JavaUtils;
import com.btxtech.game.jsre.client.common.Line2I;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 11.03.2016.
 */
public class Polygon2I {
    private List<Index> corners = new ArrayList<>();
    private List<Line2I> lines = new ArrayList<>();

    public Polygon2I(List<Index> corners) {
        this.corners = new ArrayList<>(corners);
        for (int i = 0; i < corners.size(); i++) {
            Index start = corners.get(i);
            Index end = corners.get(i + 1 < corners.size() ? i + 1 : i - corners.size() + 1);
            Line2I line = new Line2I(start, end);
            // TODO line.setNorm(end.rotateCounterClock(start, MathHelper.QUARTER_RADIANT).normalize());
            lines.add(line);
        }
    }

    public boolean isInside(Index position) {
        int i, j;
        boolean c = false;
        for (i = 0, j = corners.size() - 1; i < corners.size(); j = i++) {
            Index start = corners.get(i);
            Index end = corners.get(j);

            if (((start.getY() > position.getY()) != (end.getY() > position.getY())) && (position.getX() < (end.getX() - start.getX()) * (position.getY() - start.getY()) / (end.getY() - start.getY()) + start.getX())) {
                c = !c;
            }
        }
        return c;
    }

    public boolean adjoins(Polygon2I other) {
        for (Index corner : getCorners()) {
            if (other.isInside(corner)) {
                return true;
            }
        }
        for (Index corner : other.getCorners()) {
            if (isInside(corner)) {
                return true;
            }
        }
        return false;
    }

    public Polygon2I combine(Polygon2I other) {
        List<Index> corners = new ArrayList<>(this.corners);
        corners.addAll(other.getCorners());
        return new Polygon2I(ConvexHull.convexHull2I(corners));
    }

    public List<Line2I> getLines() {
        return lines;
    }

    public List<Index> getCorners() {
        return corners;
    }

    public int size() {
        return corners.size();
    }

//    public boolean isLineCrossing(Line2I testLine) {
//        for (Line2I line : lines) {
//            if (MathHelper.compareWithPrecision(line.getM(), testLine.getM(), 0.00001)) {
//                continue;
//            }
//            DecimalPosition cross = line.getCrossInclusive(testLine);
//            if (cross != null && !cross.equalsDelta(testLine.getPoint1()) && !cross.equalsDelta(testLine.getPoint2()) && !cross.equalsDelta(line.getPoint1()) && !cross.equalsDelta(line.getPoint2())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public double getInnerAngle(int index) {
        return getCorner(index).getAngle(getCorner(index + 1), getCorner(index - 1));
    }

    public Index getCorner(int index) {
        return corners.get(getCorrectedIndex(index));
    }

    public int getCorrectedIndex(int index) {
        return JavaUtils.getCorrectedIndex(index, corners.size());
    }

    public Polygon2I translate(Index translation) {
        List<Index> movedCorners = new ArrayList<>();
        for (Index corner : corners) {
            movedCorners.add(corner.add(translation));
        }
        return new Polygon2I(movedCorners);
    }

    public static boolean isCounterClock(List<Index> corners) {
        double angleSum = 0;
        for (int i = 0; i < corners.size(); i++) {
            Index lastCorner = corners.get(JavaUtils.getCorrectedIndex(i - 1, corners.size()));
            Index currentCorner = corners.get(JavaUtils.getCorrectedIndex(i, corners.size()));
            Index nextCorner = corners.get(JavaUtils.getCorrectedIndex(i + 1, corners.size()));
            angleSum += currentCorner.getAngle(nextCorner, lastCorner);
        }
        double angleSumCalculated = MathHelper.HALF_RADIANT * (corners.size() - 2);
        double outerAngleSumCalculated = MathHelper.ONE_RADIANT * corners.size() - angleSumCalculated;

        if (MathHelper.compareWithPrecision(angleSumCalculated, angleSum)) {
            return false;
        } else if (MathHelper.compareWithPrecision(outerAngleSumCalculated, angleSum)) {
            return true;
        } else {
            throw new IllegalStateException("angleSum is odd: " + angleSum + " (" + MathHelper.radToGrad(angleSum) + ") ");
        }
    }

    public String testString() {
        String s = " new Polygon(";
        for (Iterator<Index> iterator = corners.iterator(); iterator.hasNext(); ) {
            s += iterator.next().testString();
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += ")";
        return s;
    }
}
