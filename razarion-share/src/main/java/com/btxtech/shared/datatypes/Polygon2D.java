package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 11.03.2016.
 */
public class Polygon2D {

    // private Logger logger = Logger.getLogger(Polygon2D.class.getName());
    private List<DecimalPosition> corners = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();

    /**
     * Used by Errai
     */
    public Polygon2D() {

    }

    public Polygon2D(List<DecimalPosition> corners) {
        this.corners = new ArrayList<>(corners);
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition start = corners.get(i);
            DecimalPosition end = corners.get(i + 1 < corners.size() ? i + 1 : i - corners.size() + 1);
            Line line = new Line(start, end);
            line.setNorm(end.rotateCounterClock(start, MathHelper.QUARTER_RADIANT).normalize());
            lines.add(line);
        }
    }

    public boolean isInside(DecimalPosition position) {
        int i, j;
        boolean c = false;
        for (i = 0, j = corners.size() - 1; i < corners.size(); j = i++) {
            DecimalPosition start = corners.get(i);
            DecimalPosition end = corners.get(j);

            if (((start.getY() > position.getY()) != (end.getY() > position.getY())) && (position.getX() < (end.getX() - start.getX()) * (position.getY() - start.getY()) / (end.getY() - start.getY()) + start.getX())) {
                c = !c;
            }
        }
        return c;
    }

    public int insideCornerCount(Rectangle2D rect, double shrink) {
        Rectangle2D shrunken = rect.shrink(shrink);
        Collection<DecimalPosition> positions = shrunken.toCorners();
        int insideCornerCount = 0;
        for (DecimalPosition position : positions) {
            if (isInside(position)) {
                insideCornerCount++;
            }
        }
        return insideCornerCount;
    }

    public boolean isInside(Collection<DecimalPosition> positions) {
        for (DecimalPosition position : positions) {
            if (!isInside(position)) {
                return false;
            }
        }
        return true;
    }

    public boolean isOneCornerInside(Collection<DecimalPosition> positions) {
        for (DecimalPosition position : positions) {
            if (isInside(position)) {
                return true;
            }
        }
        return false;
    }

    public InsideCheckResult checkInside(Rectangle2D rectangle2D) {
        List<DecimalPosition> corners = rectangle2D.toCorners();
        int insideCornerCount = 0;
        for (DecimalPosition position : corners) {
            if (isInside(position)) {
                insideCornerCount++;
            }
        }
        if (insideCornerCount == 4) {
            if (isLineCrossing(rectangle2D.toLines())) {
                return InsideCheckResult.PARTLY;
            } else {
                return InsideCheckResult.INSIDE;
            }
        }
        if (insideCornerCount == 0) {
            if (isLineCrossing(rectangle2D.toLines())) {
                return InsideCheckResult.PARTLY;
            } else {
                if (rectangle2D.contains(this.corners)) {
                    return InsideCheckResult.PARTLY;
                } else {
                    return InsideCheckResult.OUTSIDE;
                }
            }
        }
        return InsideCheckResult.PARTLY;
    }

    /**
     * Returns true if polygons do cross. Returns false if one polygon is inside the other.
     *
     * @param other Polygon
     * @return true if crossing
     */
    public boolean isLineCrossing(Polygon2D other) {
        return isLineCrossing(other.getLines());
    }

    public boolean isLineCrossing(Collection<Line> otherLines) {
        for (Line line : lines) {
            for (Line otherLine : otherLines) {
                if (line.getCrossInclusive(otherLine) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public double getInnerAngle(int index) {
        return getCorner(index).angle(getCorner(index + 1), getCorner(index - 1));
    }

    public DecimalPosition getCorner(int index) {
        return corners.get(getCorrectedIndex(index));
    }

    public int getCorrectedIndex(int index) {
        int correctedIndex = index % corners.size();
        if (correctedIndex < 0) {
            correctedIndex += corners.size();
        }
        return correctedIndex;
    }


    public boolean adjoins(Polygon2D other) {
        for (DecimalPosition corner : getCorners()) {
            if (other.isInside(corner)) {
                return true;
            }
        }
        for (DecimalPosition corner : other.getCorners()) {
            if (isInside(corner)) {
                return true;
            }
        }
        return false;
    }

    public List<Line> getLines() {
        return lines;
    }

    public List<DecimalPosition> getCorners() {
        return corners;
    }

    public int size() {
        return corners.size();
    }

    public Polygon2D createReducedPolygon(int indexToReduce) {
        List<DecimalPosition> corners = new ArrayList<>(this.corners);
        corners.remove(indexToReduce);
        return new Polygon2D(corners);
    }

    public Polygon2D translate(DecimalPosition translation) {
        List<DecimalPosition> movedCorners = new ArrayList<>();
        for (DecimalPosition corner : corners) {
            movedCorners.add(corner.add(translation));
        }
        return new Polygon2D(movedCorners);
    }

    public Polygon2D combine(Polygon2D other) {
        List<Integer> thisInsideOthers = other.getInsideCorners(this);
        List<Integer> othersInsideThis = getInsideCorners(other);

        if ((othersInsideThis.isEmpty() || othersInsideThis.size() == other.size()) && (thisInsideOthers.isEmpty() || thisInsideOthers.size() == size())) {
            throw new IllegalArgumentException("Polygons do not adjoin or one is completely inside the other");
        }

        List<DecimalPosition> combined;
        if (thisInsideOthers.size() > othersInsideThis.size()) {
            combined = combineIfThisMoreCovered(thisInsideOthers, other, othersInsideThis);
        } else {
            combined = other.combineIfThisMoreCovered(othersInsideThis, this, thisInsideOthers);
        }

        return new Polygon2D(combined);
    }

    private List<Integer> getInsideCorners(Polygon2D other) {
        List<Integer> insideCorners = new ArrayList<>();
        for (int i = 0; i < other.getCorners().size(); i++) {
            DecimalPosition otherCorner = other.getCorners().get(i);
            if (isInside(otherCorner)) {
                insideCorners.add(i);
            }
        }
        return insideCorners;
    }

    private List<DecimalPosition> combineIfThisMoreCovered(List<Integer> thisInsideOthers, Polygon2D other, List<Integer> othersInsideThis) {
        int thisValidCorners = size() - thisInsideOthers.size();
        int otherValidCorners = other.size() - othersInsideThis.size();
        List<DecimalPosition> combined = new ArrayList<>();

        PairHolder<Integer> thisBeginEnd = getBeginEnd(thisInsideOthers);
        int thisBegin = thisBeginEnd.getO1();
        int thisEnd = thisBeginEnd.getO2();

        // Other begin - end
        int otherBegin;
        int otherEnd;
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
        // Combine
        // Add valid this corners
        for (int i = thisBegin; i < thisValidCorners + thisBegin; i++) {
            combined.add(getCorner(i));
        }
        // Add cross 1
        Line thisEndLine = new Line(getCorner(thisEnd), getCorner(thisEnd + 1));
        Line otherBeginLine = new Line(other.getCorner(otherBegin - 1), other.getCorner(otherBegin));
        DecimalPosition cross = thisEndLine.getCrossInclusive(otherBeginLine);
        if (cross == null) {
            throw new IllegalArgumentException("cross == null");
        }
        combined.add(cross);
        // Add valid other corners
        for (int i = otherBegin; i < otherValidCorners + otherBegin; i++) {
            combined.add(other.getCorner(i));
        }
        // Add cross 2
        Line otherEndLine = new Line(other.getCorner(otherEnd), other.getCorner(otherEnd + 1));
        Line thisBeginLine = new Line(getCorner(thisBegin - 1), getCorner(thisBegin));
        cross = otherEndLine.getCrossInclusive(thisBeginLine);
        if (cross == null) {
            throw new IllegalArgumentException("cross == null");
        }
        combined.add(cross);
        return combined;
    }

    private PairHolder<Integer> getBeginEnd(List<Integer> insideIndices) {
        int thisBegin = -1;
        int thisEnd = -1;
        for (int i = size() - 1; i >= 0; i--) {
            if (insideIndices.contains(i)) {
                if (!insideIndices.contains(CollectionUtils.getCorrectedIndex(i - 1, size()))) {
                    thisEnd = CollectionUtils.getCorrectedIndex(i - 1, size());
                }
            } else {
                if (insideIndices.contains(CollectionUtils.getCorrectedIndex(i - 1, size()))) {
                    thisBegin = i;
                }
            }
        }
        if (thisBegin < 0 || thisEnd < 0) {
            throw new IllegalArgumentException("begin or end not found");
        }
        return new PairHolder<>(thisBegin, thisEnd);
    }

    private PairHolder<Integer> getCrossingIndices(DecimalPosition p1, DecimalPosition p2) {
        Line line = new Line(p1, p2);
        for (int i = 0; i < lines.size(); i++) {
            Line polyLine = lines.get(i);
            if (polyLine.getCrossInclusive(line) != null) {
                return new PairHolder<>(i, getCorrectedIndex(i + 1));
            }
        }
        throw new IllegalArgumentException("Given points do not cross");
    }

    //
//    boolean pnpoly(int nvert, float[] vertx, float[] verty, float testx, float testy) {
//        int i, j;
//        boolean c = false;
//        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
//            if (((verty[i] > testy) != (verty[j] > testy)) && (testx < (vertx[j] - vertx[i]) * (testy - verty[i]) / (verty[j] - verty[i]) + vertx[i]))
//                c = !c;
//        }
//        return c;
//    }

    /**
     * Subtracts given polygon from this. Sort of stamp out.
     *
     * @param other polygon
     * @return null if this polygon is completely removed. (Other swallows it)
     * @throws IllegalArgumentException if the polygon do not overlap
     */
    public Polygon2D remove(Polygon2D other) {
        List<Integer> thisInsideOthers = other.getInsideCorners(this);
        int thisValidCorners = size() - thisInsideOthers.size();
        List<Integer> othersInsideThis = getInsideCorners(other);

        if (thisInsideOthers.size() == size()) {
            return null;
        } else if (thisInsideOthers.isEmpty()) {
            if (othersInsideThis.isEmpty()) {
                throw new IllegalArgumentException("Polygons do not overlap");
            } else if (othersInsideThis.size() == other.size()) {
                throw new IllegalArgumentException("Other polygon completely inside. Making wholes not allowed");
            }
        }

        List<DecimalPosition> remaining = new ArrayList<>();
        if (thisInsideOthers.isEmpty()) {
            PairHolder<Integer> otherBeginEnd = other.getBeginEnd(othersInsideThis);
            int otherBegin = otherBeginEnd.getO1();
            int otherEnd = otherBeginEnd.getO2();

            // This begin - end
            PairHolder<Integer> crossIndices = getCrossingIndices(other.getCorner(otherEnd), other.getCorner(otherEnd + 1));
            int thisBegin = crossIndices.getO2();
            int thisEnd = crossIndices.getO1();

            // Combine
            // Add valid this corners
            for (int i = thisBegin; i < thisValidCorners + thisBegin; i++) {
                remaining.add(getCorner(i));
            }

            // Add cross 1
            Line thisLine = new Line(getCorner(thisBegin), getCorner(thisEnd));
            Line otherBeginLine = new Line(other.getCorner(otherBegin - 1), other.getCorner(otherBegin));
            DecimalPosition cross = thisLine.getCrossInclusive(otherBeginLine);
            if (cross == null) {
                throw new IllegalArgumentException("cross == null");
            }
            remaining.add(cross);

            // Add stamp out other corners
            for (int i = 1; i < othersInsideThis.size() + 1; i++) {
                remaining.add(other.getCorner(otherBegin - i));
            }

            // Add cross 2
            Line otherEndLine = new Line(other.getCorner(otherEnd), other.getCorner(otherEnd + 1));
            cross = thisLine.getCrossInclusive(otherEndLine);
            if (cross == null) {
                throw new IllegalArgumentException("cross == null");
            }
            remaining.add(cross);
        } else {
            PairHolder<Integer> thisBeginEnd = getBeginEnd(thisInsideOthers);
            int thisBegin = thisBeginEnd.getO1();
            int thisEnd = thisBeginEnd.getO2();

            // Other begin - end
            int otherBegin;
            int otherEnd;
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

            // Combine
            // Add valid this corners
            for (int i = thisBegin; i < thisValidCorners + thisBegin; i++) {
                remaining.add(getCorner(i));
            }

            // Add cross 1
            Line thisLineEnd = new Line(getCorner(thisEnd), getCorner(thisEnd + 1));
            Line otherBeginLine = new Line(other.getCorner(otherBegin - 1), other.getCorner(otherBegin));
            DecimalPosition cross = thisLineEnd.getCrossInclusive(otherBeginLine);
            if (cross == null) {
                throw new IllegalArgumentException("cross == null");
            }
            remaining.add(cross);

            // Add stamp out other corners
            for (int i = 1; i < othersInsideThis.size() + 1; i++) {
                remaining.add(other.getCorner(otherBegin - i));
            }

            // Add cross 2
            Line thisLineBegin = new Line(getCorner(thisBegin - 1), getCorner(thisBegin));
            Line otherBeginEnd = new Line(other.getCorner(otherEnd + 1), other.getCorner(otherEnd));
            cross = thisLineBegin.getCrossInclusive(otherBeginEnd);
            if (cross == null) {
                throw new IllegalArgumentException("cross == null");
            }
            remaining.add(cross);
        }
        return new Polygon2D(remaining);
    }

    public Rectangle2D toAabb() {
        DecimalPosition[] array = corners.toArray(new DecimalPosition[corners.size()]);
        DecimalPosition smallest = DecimalPosition.getSmallestAabb(array);
        DecimalPosition biggest = DecimalPosition.getBiggestAabb(array);
        return new Rectangle2D(smallest, biggest);
    }

    @Override
    public String toString() {
        return "Polygon2D{" +
                "corners=" + corners +
                ", lines=" + lines +
                '}';
    }

    public String testString() {
        String testString = "new Polygon2D(Arrays.asList(";
        for (Iterator<DecimalPosition> iterator = corners.iterator(); iterator.hasNext(); ) {
            DecimalPosition corner = iterator.next();
            testString += corner.testString();
            if (iterator.hasNext()) {
                testString += ", ";
            }
        }
        testString += "))";
        return testString;
    }

    public static Polygon2D fromRectangle(double x, double y, double width, double height) {
        return new Polygon2D(Arrays.asList(new DecimalPosition(x, y), new DecimalPosition(x + width, y), new DecimalPosition(x + width, y + height), new DecimalPosition(x, y + height)));
    }
}
