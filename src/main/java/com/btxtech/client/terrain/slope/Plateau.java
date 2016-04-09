package com.btxtech.client.terrain.slope;

import com.btxtech.client.renderer.model.GroundMesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.primitives.Polygon2D;
import com.btxtech.shared.primitives.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
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
    private List<Index> innerLineMeshIndex;
    private List<Vertex> innerLine;
    private Polygon2D innerPolygon;
    private List<Index> outerLineMeshIndex;
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

    public void wrap(GroundMesh groundMeshSplatting) {
        mesh = new Mesh(xVertices, shapeTemplate.getShape().getVertexCount());
        innerLineMeshIndex = new ArrayList<>();
        outerLineMeshIndex = new ArrayList<>();
        shapeTemplate.generateMesh(mesh, borders, innerLineMeshIndex, outerLineMeshIndex, groundMeshSplatting);
        mesh.setupValues();
        // Setup helper lists
        innerLine = new ArrayList<>();
        innerPolygon = correctAndCreateEdge(innerLineMeshIndex, innerLine);
        outerLine = new ArrayList<>();
        outerPolygon = correctAndCreateEdge(outerLineMeshIndex, outerLine);
    }

    private Polygon2D correctAndCreateEdge(List<Index> indices, List<Vertex> vertices) {
        Vertex last = null;
        for (Iterator<Index> iterator = indices.iterator(); iterator.hasNext(); ) {
            Index index = iterator.next();
            Vertex current = mesh.getVertexSave(index);
            if (last != null) {
                 if (current.toXY().getDistance(last.toXY()) > 0.1) {
                    vertices.add(current);
                 } else {
                     iterator.remove();
                 }
            } else {
                vertices.add(current);
            }
            last = current;
        }
        return new Polygon2D(Vertex.toXY(vertices));
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

    public List<Index> getInnerLineMeshIndex() {
        return innerLineMeshIndex;
    }

    public List<Index> getOuterLineMeshIndex() {
        return outerLineMeshIndex;
    }

    public double getZInner() {
        return shapeTemplate.getShape().getZInner();
    }

    public List<Vertex> getInnerLine() {
        return innerLine;
    }

    public List<Vertex> getOuterLine() {
        return outerLine;
    }
}
