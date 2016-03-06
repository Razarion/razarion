package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Line;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.game.jsre.common.gameengine.syncObjects.CollisionConstants;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Plateau {
    private final ShapeTemplate shapeTemplate;
    private final int verticalSpace;
    private List<AbstractBorder> borders = new ArrayList<>();
    private Mesh mesh;
    private int xVertices;
    private List<Vertex> innerLine;
    private List<Vertex> outerLine;

    public Plateau(ShapeTemplate shapeTemplate, int verticalSpace, List<DecimalPosition> corners) {
        this.shapeTemplate = shapeTemplate;
        this.verticalSpace = verticalSpace;

        if (shapeTemplate.getDistance() > 0) {
            setupSlopingBorder(corners);
        } else {
            setupStraightBorder(corners);
        }

        // Setup vertical segments
        xVertices = 0;
        for (AbstractBorder border : borders) {
            xVertices += border.setupVerticalSegments(verticalSpace);
        }
    }

    private void setupStraightBorder(List<DecimalPosition> corners) {
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get((i + 1) % corners.size());
            borders.add(new LineBorder(current, next));
        }
    }

    private void setupSlopingBorder(List<DecimalPosition> corners) {
        // Setup inner and outer corner
        List<AbstractCornerBorder> cornerBorders = new ArrayList<>();
        for (int i = 0; i < corners.size(); i++) {
            DecimalPosition previous = corners.get((i - 1 + corners.size()) % corners.size());
            DecimalPosition current = corners.get(i);
            DecimalPosition next = corners.get((i + 1) % corners.size());
            if (current.getAngle(next, previous) > MathHelper.HALF_RADIANT) {
                cornerBorders.add(new OuterCornerBorder(current, previous, next, shapeTemplate.getDistance()));
            } else {
                cornerBorders.add(new InnerCornerBorder(current, previous, next, shapeTemplate.getDistance()));
            }
        }
        // Setup whole contour
        for (int i = 0; i < cornerBorders.size(); i++) {
            AbstractCornerBorder current = cornerBorders.get(i);
            AbstractCornerBorder next = cornerBorders.get((i + 1) % cornerBorders.size());
            borders.add(current);
            borders.add(new LineBorder(current, next, shapeTemplate.getDistance()));
        }
    }

    public void wrap() {
        innerLine = new ArrayList<>();
        outerLine = new ArrayList<>();
        mesh = new Mesh(xVertices, shapeTemplate.getShape().getVertexCount());
        shapeTemplate.generateMesh(mesh, borders, innerLine, outerLine);
        mesh.setupValues();
    }

    public Mesh getMesh() {
        return mesh;
    }

    public boolean isInside(Vertex vertex) {
        Collection<DecimalPosition> crossPoints = new ArrayList<>();
        DecimalPosition position = new DecimalPosition(vertex.getX(), vertex.getY());
        Line testLine = new Line(position, MathHelper.EIGHTH_RADIANT, Integer.MAX_VALUE);
        for (int i = 0; i < outerLine.size(); i++) {
            Vertex vertexStart = outerLine.get(i);
            Vertex vertexEnd = outerLine.get(i + 1 < outerLine.size() ? i + 1 : i - outerLine.size() + 1);
            // TODO ugly hack
            if(vertexStart.equals(vertexEnd)) {
                continue;
            }
            // TODO ugly hack ends
            Line line = new Line(new DecimalPosition(vertexStart.getX(), vertexStart.getY()), new DecimalPosition(vertexEnd.getX(), vertexEnd.getY()));

            if (line.isPointInLineInclusive(position)) {
                return true;
                // throw new IllegalStateException("Point is on line. Don't know what to do...");
            }
            DecimalPosition crossPoint = line.getCrossInfinite(testLine);
            if (crossPoint != null) {
                if (line.isPointInLineInclusive(crossPoint) && testLine.isPointInLineInclusive(crossPoint)) {
                    crossPoints.add(crossPoint);
                }
            }
        }

        crossPoints = DecimalPosition.removeSimilarPoints(crossPoints, CollisionConstants.SAFETY_DISTANCE);
        return crossPoints.size() % 2 != 0;
    }

    public List<Vertex> getInnerLine() {
        return innerLine;
    }

    public List<Vertex> getOuterLine() {
        return outerLine;
    }
}
