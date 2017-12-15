package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 21.10.2017.
 */
public class PositionMarker {
    private Collection<DecimalPosition> positions = new ArrayList<>();
    private Collection<Circle2D> circles = new ArrayList<>();
    private List<DecimalPosition> line;
    private List<Polygon2D> polygon2Ds = new ArrayList<>();
    private List<RectColor> rectangle2Ds = new ArrayList<>();

    public PositionMarker addPosition(DecimalPosition positions) {
        positions.add(positions);
        return this;
    }

    public PositionMarker addCircle(Circle2D circle) {
        circles.add(circle);
        return this;
    }

    public Collection<DecimalPosition> getPositions() {
        return positions;
    }

    public Collection<Circle2D> getCircles() {
        return circles;
    }

    public List<DecimalPosition> getLine() {
        return line;
    }

    public PositionMarker setLine(List<DecimalPosition> line) {
        this.line = line;
        return this;
    }

    public PositionMarker addPolygon2D(Polygon2D polygon) {
        polygon2Ds.add(polygon);
        return this;
    }

    public List<Polygon2D> getPolygon2Ds() {
        return polygon2Ds;
    }

    public List<RectColor> getRectangle2Ds() {
        return rectangle2Ds;
    }

    public PositionMarker addRectangle2Ds(Rectangle2D rectangle2D, Color color) {
        rectangle2Ds.add(new RectColor(rectangle2D, color));
        return this;
    }

    public static class RectColor {
        private Rectangle2D rectangle2D;
        private Color color;

        public RectColor(Rectangle2D rectangle2D, Color color) {
            this.rectangle2D = rectangle2D;
            this.color = color;
        }

        public Rectangle2D getRectangle2D() {
            return rectangle2D;
        }

        public Color getColor() {
            return color;
        }
    }
}
