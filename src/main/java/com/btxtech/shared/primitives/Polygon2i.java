package com.btxtech.shared.primitives;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.game.jsre.client.common.Line2I;
import com.btxtech.game.jsre.common.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
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

    public boolean isInside(DecimalPosition position) {
        int i, j;
        boolean c = false;
        for (i = 0, j = corners.size() - 1; i < corners.size(); j = i++) {
            Index start = corners.get(i);
            Index end = corners.get(j);

            if (((start.getY() > position.getY()) != (end.getY() > position.getY()))
                    && (position.getX() < (end.getX() - start.getX()) * (position.getY() - start.getY()) / (end.getY() - start.getY()) + start.getX()))
                c = !c;
        }
        return c;
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
        int correctedIndex = index % corners.size();
        if (correctedIndex < 0) {
            correctedIndex += corners.size();
        }
        return correctedIndex;
    }

}
