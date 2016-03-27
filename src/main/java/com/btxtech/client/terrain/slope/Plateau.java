package com.btxtech.client.terrain.slope;

import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 23.01.2016.
 */
public class Plateau {
    private final ShapeTemplate shapeTemplate;
    private List<AbstractBorder> borders = new ArrayList<>();
    private Mesh mesh;
    private int xVertices;
    private List<Vertex> innerLine;
    private Polygon2D innerPolygon;
    private List<Vertex> outerLine;
    private Polygon2D outerPolygon;

    public Plateau(ShapeTemplate shapeTemplate, int verticalSpace, List<DecimalPosition> corners) {
        this.shapeTemplate = shapeTemplate;

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
        innerPolygon = new Polygon2D(Vertex.toXY(innerLine));
        outerPolygon = new Polygon2D(Vertex.toXY(outerLine));
        mesh.setupValues();
    }

    public Mesh getMesh() {
        return mesh;
    }

    public boolean isInsideInner(Vertex vertex) {
        return innerPolygon.isInside(vertex.toXY());
    }

    public boolean isInsideOuter(Vertex vertex) {
        return outerPolygon.isInside(vertex.toXY());
    }

    public Polygon2D getInnerPolygon() {
        return innerPolygon;
    }

    public List<Vertex> getInnerLine() {
        return innerLine;
    }

    public List<Vertex> getOuterLine() {
        return outerLine;
    }

    public double getZInner() {
        return shapeTemplate.getShape().getZInner();
    }
}
