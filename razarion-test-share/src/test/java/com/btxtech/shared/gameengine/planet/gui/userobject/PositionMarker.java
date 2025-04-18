package com.btxtech.shared.gameengine.planet.gui.userobject;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
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
    private List<LineColor> lines = new ArrayList<>();
    private List<Polygon2D> polygon2Ds = new ArrayList<>();
    private List<SyncItem> syncItems = new ArrayList<>();
    private List<RectColor> rectangle2Ds = new ArrayList<>();
    private List<CircleColor> circleColors = new ArrayList<>();
    private List<PathColor> pathColors = new ArrayList<>();

    public PositionMarker addPosition(DecimalPosition position) {
        positions.add(position);
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

    public List<LineColor> getLines() {
        return lines;
    }

    public PositionMarker addLine(Line line, Color color) {
        lines.add(new LineColor(line, color));
        return this;
    }

    public PositionMarker addPolygon2D(Polygon2D polygon) {
        polygon2Ds.add(polygon);
        return this;
    }

    public List<Polygon2D> getPolygon2Ds() {
        return polygon2Ds;
    }

    public List<SyncItem> getSyncItems() {
        return syncItems;
    }

    public PositionMarker addSyncItem(com.btxtech.shared.gameengine.planet.model.SyncItem syncItem, Color color) {
        syncItems.add(new SyncItem(syncItem, color));
        return this;
    }

    public List<CircleColor> getCircleColors() {
        return circleColors;
    }

    public PositionMarker addCircleColor(Circle2D circle2D, Color color) {
        circleColors.add(new CircleColor(circle2D, color));
        return this;
    }

    public List<RectColor> getRectangle2Ds() {
        return rectangle2Ds;
    }

    public PositionMarker addRectangle2D(Rectangle2D rectangle2D, Color color) {
        rectangle2Ds.add(new RectColor(rectangle2D, color));
        return this;
    }

    public List<PathColor> getPathColors() {
        return pathColors;
    }

    public PositionMarker addPath(List<DecimalPosition> path, Color color) {
        pathColors.add(new PathColor(path, color));
        return this;
    }

    public static class SyncItem {
        private Circle2D circle2D;
        private Color color;

        public SyncItem(com.btxtech.shared.gameengine.planet.model.SyncItem syncItem, Color color) {
            circle2D = new Circle2D(syncItem.getAbstractSyncPhysical().getPosition(), syncItem.getSyncPhysicalMovable().getRadius());
            this.color = color;
        }

        public Circle2D getCircle2D() {
            return circle2D;
        }

        public Color getColor() {
            return color;
        }
    }

    public static class CircleColor {
        private Circle2D circle2D;
        private Color color;

        public CircleColor(Circle2D circle2D, Color color) {
            this.circle2D = circle2D;
            this.color = color;
        }

        public Circle2D getCircle2D() {
            return circle2D;
        }

        public Color getColor() {
            return color;
        }
    }

    public static class LineColor {
        private Line line;
        private Color color;

        public LineColor(Line line, Color color) {
            this.line = line;
            this.color = color;
        }

        public Line getLine() {
            return line;
        }

        public Color getColor() {
            return color;
        }
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

    public static class PathColor {
        private List<DecimalPosition> path;
        private Color color;

        public PathColor(List<DecimalPosition> path, Color color) {
            this.path = path;
            this.color = color;
        }

        public List<DecimalPosition> getPath() {
            return path;
        }

        public Color getColor() {
            return color;
        }
    }
}
