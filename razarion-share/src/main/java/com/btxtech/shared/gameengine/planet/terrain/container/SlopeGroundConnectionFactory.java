package com.btxtech.shared.gameengine.planet.terrain.container;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.slope.Driveway;
import com.btxtech.shared.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.btxtech.shared.datatypes.Vertex.toXY;

public class SlopeGroundConnectionFactory {

    public static List<List<Vertex>> setupSlopeGroundConnection(Rectangle2D absoluteRect, List<List<DecimalPosition>> piercingLines, double groundHeight, boolean water, Driveway driveway, double drivewayBaseHeight) {
        if (piercingLines == null || piercingLines.isEmpty()) {
            return null;
        }
        List<List<Vertex>> groundConnections = piercingLines.stream()
                .map(piercingLine -> setupSlopeGroundConnection1(absoluteRect, piercingLine, groundHeight, water, driveway, drivewayBaseHeight))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (groundConnections.size() < 2) {
            return groundConnections;
        }

        boolean adjoins = false;
        for (int i = 0; i < groundConnections.size() && !adjoins; i++) {
            for (int j = i + 1; j < groundConnections.size() && !adjoins; j++) {
                Polygon2D a = new Polygon2D(toXY(groundConnections.get(i)));
                Polygon2D b = new Polygon2D(toXY(groundConnections.get(j)));
                if (a.adjoins(b)) {
                    adjoins = true;
                }
            }
        }

        if (adjoins) {
            return Collections.singletonList(setupSlopeGroundConnection2(absoluteRect, piercingLines, groundHeight, water, driveway, drivewayBaseHeight));
        } else {
            return groundConnections;
        }


        // printTeatCase(absoluteRect, piercingLines, groundHeight, water, result);
    }

    private static List<Vertex> setupSlopeGroundConnection1(Rectangle2D absoluteRect, List<DecimalPosition> piercingLine, double groundHeight, boolean water, Driveway driveway, double drivewayBaseHeight) {
        if (water) {
            piercingLine = new ArrayList<>(piercingLine);
            Collections.reverse(piercingLine);
        }
        List<Vertex> polygon = new ArrayList<>();

        RectanglePiercing startRectanglePiercing;
        RectanglePiercing endRectanglePiercing;
        if (piercingLine.size() == 2) {
            // This is a left out node
            Line crossLine = new Line(piercingLine.get(0), piercingLine.get(1));
            Collection<DecimalPosition> crossPoints = absoluteRect.getCrossPointsLine(crossLine);
            if (crossPoints.size() == 1) {
                // Goes exactly through the corner -> return. This is may wrong
                return null;
            } else if (crossPoints.size() != 2) {
                throw new IllegalStateException("Exactly two cross points expected: " + crossPoints.size());
            }
            DecimalPosition start = DecimalPosition.getNearestPoint(piercingLine.get(0), crossPoints);
            startRectanglePiercing = getRectanglePiercing(absoluteRect, start);
            DecimalPosition end = DecimalPosition.getFurthestPoint(piercingLine.get(0), crossPoints);
            endRectanglePiercing = getRectanglePiercing(absoluteRect, end);
        } else {
            Line startLine = new Line(piercingLine.get(0), piercingLine.get(1));
            startRectanglePiercing = getRectanglePiercing(absoluteRect, startLine, piercingLine.get(0));

            Line endLine = new Line(piercingLine.get(piercingLine.size() - 2), piercingLine.get(piercingLine.size() - 1));
            endRectanglePiercing = getRectanglePiercing(absoluteRect, endLine, piercingLine.get(piercingLine.size() - 1));
        }

        addOnlyXyUnique(polygon, toVertexSlope(startRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));
        Side side = startRectanglePiercing.getSide();
        if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
            if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
                side = side.getSuccessor();
            }
        }

        while (side != endRectanglePiercing.side) {
            addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
            side = side.getSuccessor();
        }
        addOnlyXyUnique(polygon, toVertexSlope(endRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));

        for (int i = piercingLine.size() - 2; i > 0; i--) {
            addOnlyXyUnique(polygon, toVertexSlope(piercingLine.get(i), driveway, drivewayBaseHeight, groundHeight));
        }

        if (polygon.size() < 3) {
            return null;
        }
        return polygon;
    }


    private static List<Vertex> setupSlopeGroundConnection2(Rectangle2D absoluteRect, List<List<DecimalPosition>> piercingLines, double groundHeight, boolean water, Driveway driveway, double drivewayBaseHeight) {
        List<Vertex> polygon = new ArrayList<>();

        RectanglePiercing startRectanglePiercing = null;
        RectanglePiercing endRectanglePiercing = null;

        for (int i = 0; i < piercingLines.size(); i++) {
            List<DecimalPosition> piercingLine = piercingLines.get(i);

            if (water) {
                piercingLine = new ArrayList<>(piercingLine);
                Collections.reverse(piercingLine);
            }
            RectanglePiercingPair pair = setupRectanglePiercings(absoluteRect, piercingLine);
            if (pair == null) {
                // Goes exactly through the corner -> return. This is may wrong
                return null;
            }
            startRectanglePiercing = pair.start;
            endRectanglePiercing = pair.end;

            addOnlyXyUnique(polygon, toVertexSlope(endRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));

            for (int j = piercingLine.size() - 2; j > 0; j--) {
                addOnlyXyUnique(polygon, toVertexSlope(piercingLine.get(j), driveway, drivewayBaseHeight, groundHeight));
            }
            addOnlyXyUnique(polygon, toVertexSlope(startRectanglePiercing.getCross(), driveway, drivewayBaseHeight, groundHeight));

            List<DecimalPosition> nextPiercingLine = CollectionUtils.getCorrectedElement(i + 1, piercingLines);
            RectanglePiercingPair nextPair = setupRectanglePiercings(absoluteRect, nextPiercingLine);
            if (nextPair == null) {
                // Goes exactly through the corner -> return. This is may wrong
                return null;
            }
            endRectanglePiercing = nextPair.end;

            Side side = startRectanglePiercing.getSide();
            if (startRectanglePiercing.getSide() == endRectanglePiercing.getSide()) {
                if (!startRectanglePiercing.getSide().isBefore(startRectanglePiercing.getCross(), endRectanglePiercing.getCross())) {
                    addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
                    side = side.getSuccessor();
                }
            }

            while (side != endRectanglePiercing.side) {
                addOnlyXyUnique(polygon, toVertexGround(getSuccessorCorner(absoluteRect, side), driveway, drivewayBaseHeight, groundHeight, water));
                side = side.getSuccessor();
            }
        }

        if (polygon.size() < 3) {
            return null;
        }
        return polygon;
    }

    private static RectanglePiercingPair setupRectanglePiercings(Rectangle2D absoluteRect, List<DecimalPosition> piercingLine) {
        if (piercingLine.size() == 2) {
            // This is a left out node
            Line crossLine = new Line(piercingLine.get(0), piercingLine.get(1));
            Collection<DecimalPosition> crossPoints = absoluteRect.getCrossPointsLine(crossLine);
            if (crossPoints.size() == 1) {
                // Goes exactly through the corner -> return. This is may wrong
                return null;
            } else if (crossPoints.size() != 2) {
                throw new IllegalStateException("Exactly two cross points expected: " + crossPoints.size());
            }
            RectanglePiercingPair rectanglePiercingPair = new RectanglePiercingPair();
            DecimalPosition start = DecimalPosition.getNearestPoint(piercingLine.get(0), crossPoints);
            rectanglePiercingPair.start = getRectanglePiercing(absoluteRect, start);
            DecimalPosition end = DecimalPosition.getFurthestPoint(piercingLine.get(0), crossPoints);
            rectanglePiercingPair.end = getRectanglePiercing(absoluteRect, end);
            return rectanglePiercingPair;
        } else {
            RectanglePiercingPair rectanglePiercingPair = new RectanglePiercingPair();

            Line startLine = new Line(piercingLine.get(0), piercingLine.get(1));
            rectanglePiercingPair.start = getRectanglePiercing(absoluteRect, startLine, piercingLine.get(0));

            Line endLine = new Line(piercingLine.get(piercingLine.size() - 2), piercingLine.get(piercingLine.size() - 1));
            rectanglePiercingPair.end = getRectanglePiercing(absoluteRect, endLine, piercingLine.get(piercingLine.size() - 1));
            return rectanglePiercingPair;
        }
    }

    private static void addOnlyXyUnique(List<Vertex> list, Vertex vertex) {
        if (list.isEmpty()) {
            list.add(vertex);
            return;
        }
        DecimalPosition decimalPosition = vertex.toXY();
        for (Vertex existing : list) {
            if (existing.toXY().equals(decimalPosition)) {
                return;
            }
        }
        list.add(vertex);
    }

    private static Vertex toVertexGround(DecimalPosition position, Driveway driveway, double drivewayBaseHeight, double groundHeight, boolean water) {
        if (water) {
            return new Vertex(position, groundHeight);
        } else {
            double height;
            if (driveway != null) {
                height = driveway.getInterpolateDrivewayHeight(position) + drivewayBaseHeight;
            } else {
                height = groundHeight;
            }
            return new Vertex(position, height);
        }
    }

    private static Vertex toVertexSlope(DecimalPosition position, Driveway driveway, double drivewayBaseHeight, double groundHeight) {
        double height;
        if (driveway != null) {
            height = driveway.getInterpolateDrivewayHeight(position) + drivewayBaseHeight;
        } else {
            height = groundHeight;
        }
        return new Vertex(position, height);
    }

    private static RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, DecimalPosition crossPoint) {
        if (rectangle.lineW().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.WEST);
        }
        if (rectangle.lineS().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.SOUTH);
        }
        if (rectangle.lineE().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.EAST);
        }
        if (rectangle.lineN().isPointInLineInclusive(crossPoint)) {
            return new RectanglePiercing(crossPoint, Side.NORTH);
        }
        throw new IllegalArgumentException("getRectanglePiercing should not happen 2");
    }

    private static RectanglePiercing getRectanglePiercing(Rectangle2D rectangle, Line line, DecimalPosition reference) {
        int crossPoints = rectangle.getCrossPointsLine(line).size();
        if (crossPoints == 0) {
            throw new IllegalArgumentException("getRectanglePiercing should not happen 1");
        }
        boolean ambiguous = crossPoints > 1;

        double minDistance = Double.MAX_VALUE;
        DecimalPosition bestFitCrossPoint = null;
        Side bestFitSide = null;
        DecimalPosition crossPoint = rectangle.lineW().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.WEST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.WEST);
            }
        }
        crossPoint = rectangle.lineS().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.SOUTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.SOUTH);
            }
        }
        crossPoint = rectangle.lineE().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.EAST;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.EAST);
            }
        }
        crossPoint = rectangle.lineN().getCrossInclusive(line);
        if (crossPoint != null) {
            if (ambiguous) {
                double distance = crossPoint.getDistance(reference);
                if (distance < minDistance) {
                    bestFitCrossPoint = crossPoint;
                    bestFitSide = Side.NORTH;
                }
            } else {
                return new RectanglePiercing(crossPoint, Side.NORTH);
            }
        }
        if (ambiguous) {
            return new RectanglePiercing(bestFitCrossPoint, bestFitSide);
        } else {
            throw new IllegalArgumentException("getRectanglePiercing should not happen 2");
        }
    }

    private static DecimalPosition getSuccessorCorner(Rectangle2D rectangle, Side side) {
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

    private static void printTeatCase(Rectangle2D absoluteRect, List<List<DecimalPosition>> piercingLines, double groundHeight, boolean water, List<Vertex> result) {
        String piercingLinesString = piercingLines
                .stream()
                .map(decimalPositions -> "Arrays.asList(" + decimalPositions
                        .stream()
                        .map(decimalPosition -> "new DecimalPosition(" + decimalPosition.getX() + ", " + decimalPosition.getY() + ")")
                        .collect(Collectors.joining(",\n")) + ")")
                .collect(Collectors.joining(",\n"));

        String assertCode;
        if (result != null) {
            String resultString = result
                    .stream()
                    .map(vertes -> "new Vertex(" + vertes.getX() + ", " + vertes.getY() + ", " + vertes.getZ() + ")")
                    .collect(Collectors.joining(",\n"));
            assertCode = "        TestHelper.assertVertices(\n" +
                    "                Arrays.asList(\n" +
                    resultString +
                    "                ),\n" +
                    "                slopeGroundConnection);\n";
        } else {
            assertCode = "        Assert.assertNull(slopeGroundConnection);\n";
        }

        System.out.println("    @Test\n" +
                "    public void test() {\n" +
                "        Rectangle2D absoluteRect = new Rectangle2D(" + absoluteRect.getStart().getX() + ", " + absoluteRect.getStart().getY() + ", " + absoluteRect.width() + ", " + absoluteRect.height() + ");\n" +
                "        List<List<DecimalPosition>> piercingLines = Arrays.asList(\n" +
                piercingLinesString +
                "\n" +
                "                );\n" +


                "        List<Vertex> slopeGroundConnection = SlopeGroundConnectionFactory.setupSlopeGroundConnection(\n" +
                "        absoluteRect,\n" +
                "        piercingLines,\n" +
                "                " + groundHeight + ",\n" +
                "                " + water + ",\n" +
                "                null,\n" +
                "                0);\n" +
                "\n" +
                assertCode +
                "    }\n");
    }

    private enum Side {
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

    private static class RectanglePiercing {

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

    private static class RectanglePiercingPair {
        RectanglePiercing start;
        RectanglePiercing end;

    }

}
