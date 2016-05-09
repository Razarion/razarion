package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.JavaUtils;
import com.btxtech.game.jsre.client.common.Line2I;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.common.utils.PairHolder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 11.03.2016.
 */
public class Polygon2I {
    private Logger logger = Logger.getLogger(Polygon2I.class.getName());
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
        System.out.println("@Test");
        System.out.println("public void combine() {");
        System.out.println(" Polygon2I poly1 = " + testString() + ";");
        System.out.println(" Polygon2I poly2 = " + other.testString() + ";");

        List<Integer> thisInsideOthers = other.getInsideCorners(this);
        List<Integer> othersInsideThis = getInsideCorners(other);

        if ((othersInsideThis.isEmpty() || othersInsideThis.size() == other.size()) && (thisInsideOthers.isEmpty() || thisInsideOthers.size() == size())) {
            throw new IllegalArgumentException("Polygons do not adjoin or one is completely inside the other");
        }

        List<Index> combined;
        if (thisInsideOthers.size() > othersInsideThis.size()) {
            combined = combineIfThisMoreCovered(thisInsideOthers, other, othersInsideThis);
        } else {
            combined = other.combineIfThisMoreCovered(othersInsideThis, this, thisInsideOthers);
        }

        System.out.println(" Polygon2I polyResult = poly1.combine(poly2);");
        System.out.println(" Assert.assertEquals(" + new Polygon2I(combined).testString() + ", polyResult);");
        System.out.println("}");
        return new Polygon2I(combined);
    }

    private List<Index> combineIfThisMoreCovered(List<Integer> thisInsideOthers, Polygon2I other, List<Integer> othersInsideThis) {
        int thisValidCorners = size() - thisInsideOthers.size();
        int otherValidCorners = other.size() - othersInsideThis.size();
        List<Index> combined = new ArrayList<>();

        PairHolder<Integer> thisBeginEnd = getBeginEnd(thisInsideOthers);
        int thisBegin = thisBeginEnd.getO1();
        int thisEnd = thisBeginEnd.getO2();

        System.out.println("thisBegin: " + thisBegin);
        System.out.println("thisEnd: " + thisEnd);
        // Other begin - end
        int otherBegin = -1;
        int otherEnd = -1;
        if (othersInsideThis.isEmpty()) {
            PairHolder<Integer> crossIndices = other.getCrossingIndices(getCorner(thisEnd), getCorner(thisEnd + 1));
            otherBegin = crossIndices.getO2();
            otherEnd = crossIndices.getO1();
        } else {
            PairHolder<Integer> otherBeginEnd = other.getBeginEnd(othersInsideThis);
            otherBegin = otherBeginEnd.getO1();
            otherEnd = otherBeginEnd.getO2();
        }
        if (thisBegin < 0 || thisEnd < 0) {
            throw new IllegalArgumentException("begin or end not found");
        }
        System.out.println("otherBegin: " + otherBegin);
        System.out.println("otherEnd: " + otherEnd);
        // Combine
        // Add valid this corners
        for (int i = thisBegin; i < thisValidCorners + thisBegin; i++) {
            combined.add(getCorner(i));
        }
        // Add cross 1
        Line2I thisEndLine = new Line2I(getCorner(thisEnd), getCorner(thisEnd + 1));
        Line2I otherBeginLine = new Line2I(other.getCorner(otherBegin - 1), other.getCorner(otherBegin));
        Index cross = thisEndLine.getCrossPoint(otherBeginLine);
        if (cross == null) {
            throw new IllegalArgumentException("cross == null");
        }
        combined.add(cross);
        // Add valid other corners
        for (int i = otherBegin; i < otherValidCorners + otherBegin; i++) {
            combined.add(other.getCorner(i));
        }
        // Add cross 2
        Line2I otherEndLine = new Line2I(other.getCorner(otherEnd), other.getCorner(otherEnd + 1));
        Line2I thisBeginLine = new Line2I(getCorner(thisBegin - 1), getCorner(thisBegin));
        cross = otherEndLine.getCrossPoint(thisBeginLine);
        if (cross == null) {
            throw new IllegalArgumentException("cross == null");
        }
        combined.add(cross);
        return combined;
    }

//    System.out.println("@Test");
//    System.out.println("public void combine() {");
//    System.out.println(" Polygon2I poly1 = " + testString() + ";");
//    System.out.println(" Polygon2I poly2 = " + other.testString() + ";");
//    System.out.println(" Polygon2I polyResult = poly1.combine(poly2);");
//    System.out.println(" Assert.assertEquals(" + new Polygon2I(combined).testString() + ", polyResult);");
//    System.out.println("}");

    private PairHolder<Integer> getBeginEnd(List<Integer> insideIndices) {
        int thisBegin = -1;
        int thisEnd = -1;
        for (int i = size() - 1; i >= 0; i--) {
            if (insideIndices.contains(i)) {
                if (!insideIndices.contains(JavaUtils.getCorrectedIndex(i - 1, size()))) {
                    thisEnd = JavaUtils.getCorrectedIndex(i - 1, size());
                }
            } else {
                if (insideIndices.contains(JavaUtils.getCorrectedIndex(i - 1, size()))) {
                    thisBegin = i;
                }
            }
        }
        if (thisBegin < 0 || thisEnd < 0) {
            throw new IllegalArgumentException("begin or end not found");
        }
        return new PairHolder<>(thisBegin, thisEnd);
    }

    private PairHolder<Integer> getCrossingIndices(Index p1, Index p2) {
        Line2I line = new Line2I(p1, p2);
        for (int i = 0; i < lines.size(); i++) {
            Line2I polyLine = lines.get(i);
            if (polyLine.getCrossPoint(line) != null) {
                return new PairHolder<>(i, getCorrectedIndex(i + 1));
            }
        }
        throw new IllegalArgumentException("Given points do not cross");
    }

    private List<Integer> getInsideCorners(Polygon2I other) {
        List<Integer> insideCorners = new ArrayList<>();
        for (int i = 0; i < other.getCorners().size(); i++) {
            Index otherCorner = other.getCorners().get(i);
            if (isInside(otherCorner)) {
                insideCorners.add(i);
            }
        }
        return insideCorners;
    }

//    private void dumpCombineTestCase(Polygon2D other) {
//        logger.severe(SPACE + "@Test");
//        logger.severe(SPACE + "public void combine() {");
//        logger.severe(SPACE + " Polygon2D poly1 = " + testString() + ";");
//        logger.severe(SPACE + " Polygon2D poly2 = " + other.testString() + ";");
//        logger.severe(SPACE + " Polygon2D polyResult = poly1.combine(poly2);\n");
//        logger.severe(SPACE + "}");
//    }


    public Polygon2I hull(Polygon2I other) {
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
        String s = " new Polygon2I(Arrays.asList(";
        for (Iterator<Index> iterator = corners.iterator(); iterator.hasNext(); ) {
            s += iterator.next().testString();
            if (iterator.hasNext()) {
                s += ", ";
            }
        }
        s += "))";
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Polygon2I polygon2I = (Polygon2I) o;
        return corners.equals(polygon2I.corners);
    }

    @Override
    public int hashCode() {
        return corners.hashCode();
    }
}
