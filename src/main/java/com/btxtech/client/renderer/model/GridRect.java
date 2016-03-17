package com.btxtech.client.renderer.model;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 14.03.2016.
 */
public class GridRect {
    private Line northLine;
    private Index bottomLeftIndex;
    private Line eastLine;
    private Index bottomRightIndex;
    private Line southLine;
    private Index topRightIndex;
    private Line westLine;
    private Index topLeftIndex;

    public GridRect(Vertex bottomLeft, Index bottomLeftIndex, Vertex bottomRight, Index bottomRightIndex, Vertex topRight, Index topRightIndex, Vertex topLeft, Index topLeftIndex) {
        this.bottomLeftIndex = bottomLeftIndex;
        this.bottomRightIndex = bottomRightIndex;
        this.topRightIndex = topRightIndex;
        this.topLeftIndex = topLeftIndex;
        southLine = new Line(bottomLeft.toXY(), bottomRight.toXY());
        eastLine = new Line(bottomRight.toXY(), topRight.toXY());
        northLine = new Line(topRight.toXY(), topLeft.toXY());
        westLine = new Line(topLeft.toXY(), bottomLeft.toXY());
    }

    public Cross getSingleCross(Line line, Cross ignore) {
        List<Cross> crosses = new ArrayList<>();
        DecimalPosition crossPoint = northLine.getCrossInclusive(line);
        if (crossPoint != null) {
            crosses.add(new Cross(topLeftIndex, topRightIndex));
        }
        crossPoint = eastLine.getCrossInclusive(line);
        if (crossPoint != null) {
            crosses.add(new Cross(bottomRightIndex, topRightIndex));
        }
        crossPoint = southLine.getCrossInclusive(line);
        if (crossPoint != null) {
            crosses.add(new Cross(bottomLeftIndex, bottomRightIndex));
        }
        crossPoint = westLine.getCrossInclusive(line);
        if (crossPoint != null) {
            crosses.add(new Cross(bottomLeftIndex, topLeftIndex));
        }
        if (ignore != null) {
            crosses.remove(ignore);
        }
        if (crosses.size() > 1) {
            throw new IllegalArgumentException();
        }
        if (crosses.isEmpty()) {
            return null;
        }
        return crosses.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GridRect gridRect = (GridRect) o;

        return northLine != null ? northLine.equals(gridRect.northLine) : gridRect.northLine == null
                && (eastLine != null ? eastLine.equals(gridRect.eastLine) : gridRect.eastLine == null
                && (southLine != null ? southLine.equals(gridRect.southLine) : gridRect.southLine == null
                && (westLine != null ? westLine.equals(gridRect.westLine) : gridRect.westLine == null)));

    }

    @Override
    public int hashCode() {
        int result = northLine != null ? northLine.hashCode() : 0;
        result = 31 * result + (eastLine != null ? eastLine.hashCode() : 0);
        result = 31 * result + (southLine != null ? southLine.hashCode() : 0);
        result = 31 * result + (westLine != null ? westLine.hashCode() : 0);
        return result;
    }

    public static class Cross {
        private final Index indexStart;
        private final Index indexEnd;

        public Cross(Index indexStart, Index indexEnd) {
            this.indexStart = indexStart;
            this.indexEnd = indexEnd;
        }

        public Index getIndexStart() {
            return indexStart;
        }

        public Index getIndexEnd() {
            return indexEnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Cross cross = (Cross) o;
            return indexStart.equals(cross.indexStart) && indexEnd.equals(cross.indexEnd);

        }

        @Override
        public int hashCode() {
            int result = indexStart.hashCode();
            result = 31 * result + indexEnd.hashCode();
            return result;
        }
    }
}
