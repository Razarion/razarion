package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.scenariongui.InstanceStringGenerator;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.utils.CollectionUtils;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class SlopeGroundConnectionScenario extends Scenario {
    //List<DecimalPosition> origin = new ArrayList<>();
    List<DecimalPosition> origin = Arrays.asList(new DecimalPosition(9.917, 1.333), new DecimalPosition(11.717, 2.167), new DecimalPosition(13.050, 3.633), new DecimalPosition(13.317, 5.567), new DecimalPosition(13.117, 6.967), new DecimalPosition(12.083, 8.133), new DecimalPosition(10.383, 8.100), new DecimalPosition(8.450, 8.867), new DecimalPosition(7.950, 10.267), new DecimalPosition(7.683, 13.067), new DecimalPosition(7.017, 14.900), new DecimalPosition(4.217, 14.900), new DecimalPosition(-0.617, 18.733), new DecimalPosition(-1.650, 14.400), new DecimalPosition(-3.550, 18.300), new DecimalPosition(-4.950, 12.167), new DecimalPosition(-7.350, 9.200), new DecimalPosition(-6.683, 6.800), new DecimalPosition(-5.583, 6.567), new DecimalPosition(-3.583, 7.567), new DecimalPosition(-0.317, 4.733), new DecimalPosition(-0.250, 3.000), new DecimalPosition(-2.250, 1.200), new DecimalPosition(-5.750, -4.800), new DecimalPosition(-2.017, -8.400), new DecimalPosition(6.283, -9.333), new DecimalPosition(10.483, -13.567), new DecimalPosition(12.917, -6.900), new DecimalPosition(13.783, -3.400));
    private List<DecimalPosition> outerLine = origin;
    private Rectangle2D rect = new Rectangle2D(0, 0, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
    private DecimalPosition start;
    private List<DecimalPosition> piercingConnected;
    private List<DecimalPosition> polygonToTriangle;
    private List<Vertex> triangles;

    @Override
    public void render(ExtendedGraphicsContext context) {
        context.fillRectangle(rect, 0.1, Color.GREEN);
        context.strokePolygon(outerLine, 0.2, Color.BLUEVIOLET, false);

//        if (piercingConnected != null) {
//            context.strokeCurveDecimalPosition(piercingConnected, 0.4, Color.DARKGREY, true);
//        }

//        if (polygonToTriangle != null) {
//            context.strokeCurveDecimalPosition(polygonToTriangle, 0.2, Color.BLACK, false);
//        }

//        if (start != null) {
//            context.drawPosition(start, 1, Color.RED);
//        }

        if (triangles != null) {
            context.strokeTriangles(triangles, 0.1, Color.RED);
        }
    }

    @Override
    public void onGenerate() {
        System.out.println("------------------------------------------");
        System.out.println(InstanceStringGenerator.generateDecimalPositionList(origin));
    }

//    @Override
//    public boolean onMouseDown(DecimalPosition position) {
//        origin.add(position);
//        return true;
//    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        outerLine = new ArrayList<>(DecimalPosition.add(origin, position));
        emulate();
        return true;
    }

    // -------------ObstacleContainer-----------------
    private void emulate() {
        long time = System.nanoTime();
        try {
            start = null;
            piercingConnected = null;
            polygonToTriangle = null;
            triangles = null;
            for (int i = 0; i < outerLine.size(); i++) {
                DecimalPosition position = outerLine.get(i);
                Rectangle2D currentNodeRect = getRectangle(position);
                if (currentNodeRect != null) {
                    findConnectingPolygon(rect, i, outerLine);
                    return;
                }
            }
        } finally {
            System.out.println("Time: " + (System.nanoTime() - time) / 1000000.0);
        }
    }

    private Rectangle2D getRectangle(DecimalPosition position) {
        if (rect.contains(position)) {
            return rect;
        } else {
            return null;
        }
    }

    private Vertex toVertexGround(DecimalPosition position) {
        return new Vertex(position, 0);
    }

    private Vertex toVertexSlope(DecimalPosition position) {
        return new Vertex(position, 0);
    }

    // -------------Logic-----------------
    private void findConnectingPolygon(Rectangle2D rect, int index, List<DecimalPosition> outerLine) {
        // Find piercingConnected
        int currentIndex = findStart(rect, index, outerLine);
        start = outerLine.get(currentIndex);
        DecimalPosition current = start;
        piercingConnected = new ArrayList<>();
        piercingConnected.add(current);
        do {
            currentIndex = CollectionUtils.getCorrectedIndex(currentIndex + 1, outerLine);
            current = outerLine.get(currentIndex);
            piercingConnected.add(current);
        } while (rect.contains(current));

        // Generate Polygon
        List<Vertex> polygon = new ArrayList<>();
        Line startLine = new Line(piercingConnected.get(0), piercingConnected.get(1));
        RectanglePiercing startRectanglePiercing = getRectanglePiercing(rect, startLine);

        Line endLine = new Line(piercingConnected.get(piercingConnected.size() - 2), piercingConnected.get(piercingConnected.size() - 1));
        RectanglePiercing endRectanglePiercing = getRectanglePiercing(rect, endLine);


        polygon.add(toVertexSlope(startRectanglePiercing.getCross()));
        Side side = startRectanglePiercing.getSide();
        if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
            if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                polygon.add((toVertexGround(getSuccessorCorner(rect, side))));
                side = side.getSuccessor();
            }
        }

        while (side != endRectanglePiercing.side) {
            polygon.add((toVertexGround(getSuccessorCorner(rect, side))));
            side = side.getSuccessor();
        }
        polygon.add(toVertexSlope(endRectanglePiercing.getCross()));

        for (int i = piercingConnected.size() - 2; i > 0; i--) {
            polygon.add(toVertexSlope(piercingConnected.get(i)));
        }

        // Triangulate Polygon
        triangles = new ArrayList<>();
        Triangulator.calculate(polygon, (vertex1, vertex2, vertex3) -> {
            triangles.add(vertex1);
            triangles.add(vertex2);
            triangles.add(vertex3);
        });
    }

    private int findStart(Rectangle2D rect, int index, List<DecimalPosition> outerLine) {
        int protection = outerLine.size() + 1;
        do {
            index = CollectionUtils.getCorrectedIndex(index - 1, outerLine);
            protection--;
            if (protection < 0) {
                throw new IllegalStateException("Prevent infinite loop");
            }
        } while (rect.contains(outerLine.get(index)));
        return index;
    }

    private RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, Line line) {
        DecimalPosition crossPoint = rectangle.lineW().getCrossInclusive(line);
        if (crossPoint != null) {
            return new RectanglePiercing(crossPoint, Side.WEST);
        }
        crossPoint = rectangle.lineS().getCrossInclusive(line);
        if (crossPoint != null) {
            return new RectanglePiercing(crossPoint, Side.SOUTH);
        }
        crossPoint = rectangle.lineE().getCrossInclusive(line);
        if (crossPoint != null) {
            return new RectanglePiercing(crossPoint, Side.EAST);
        }
        crossPoint = rectangle.lineN().getCrossInclusive(line);
        if (crossPoint != null) {
            return new RectanglePiercing(crossPoint, Side.NORTH);
        }
        throw new IllegalArgumentException("getRectanglePiercing should not happen");
    }

    // -------------Helpers----------

    public static class RectanglePiercing {

        private DecimalPosition cross;
        private Side side;

        public RectanglePiercing(DecimalPosition cross, Side side) {
            this.cross = cross;
            this.side = side;
        }

        public DecimalPosition getCross() {
            return cross;
        }

        public Side getSide() {
            return side;
        }
    }


    public enum Side {
        NORTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() > position2.getX();
            }
        },
        WEST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() > position2.getY();
            }
        },
        SOUTH {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getX() < position2.getX();
            }
        },
        EAST {
            @Override
            boolean isBefore(DecimalPosition position1, DecimalPosition position2) {
                return position1.getY() < position2.getY();
            }
        };

        Side getSuccessor() {
            switch (this) {
                case NORTH:
                    return WEST;
                case WEST:
                    return SOUTH;
                case SOUTH:
                    return EAST;
                case EAST:
                    return NORTH;
                default:
                    throw new IllegalArgumentException("Side don't know how to handle: " + this);
            }
        }

        abstract boolean isBefore(DecimalPosition position1, DecimalPosition position2);
    }

    public DecimalPosition getSuccessorCorner(Rectangle2D rectangle, Side side) {
        switch (side) {
            case NORTH:
                return rectangle.cornerTopLeft();
            case WEST:
                return rectangle.cornerBottomLeft();
            case SOUTH:
                return rectangle.cornerBottomRight();
            case EAST:
                return rectangle.cornerTopRight();
            default:
                throw new IllegalArgumentException("getCorner: don't know how to handle side: " + side);
        }

    }

}
