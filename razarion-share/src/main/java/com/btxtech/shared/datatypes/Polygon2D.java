package com.btxtech.shared.datatypes;

import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.Arrays;
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

    public boolean isLineCrossing(Line testLine) {
        for (Line line : lines) {
            if (MathHelper.compareWithPrecision(line.getM(), testLine.getM(), 0.00001)) {
                continue;
            }
            DecimalPosition cross = line.getCrossInclusive(testLine);
            if (cross != null && !cross.equalsDelta(testLine.getPoint1()) && !cross.equalsDelta(testLine.getPoint2()) && !cross.equalsDelta(line.getPoint1()) && !cross.equalsDelta(line.getPoint2())) {
                return true;
            }
        }
        return false;
    }

    public boolean isLineCrossing2(Line testLine) {
        for (Line line : lines) {
            if (MathHelper.compareWithPrecision(line.getM(), testLine.getM(), 0.00001)) {
                continue;
            }
            DecimalPosition cross = line.getCrossInclusive(testLine);
            if (cross != null && !cross.equalsDelta(testLine.getPoint1(), 1.0) && !cross.equalsDelta(testLine.getPoint2(), 1.0)) {
                return true;
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
        List<DecimalPosition> corners = new ArrayList<>(this.corners);
        corners.addAll(other.getCorners());
        return new Polygon2D(ConvexHull.convexHull2D(corners));
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

    public Polygon2D remove(Polygon2D other) {
        // TODO see Polygon2I
        throw new UnsupportedOperationException();
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
