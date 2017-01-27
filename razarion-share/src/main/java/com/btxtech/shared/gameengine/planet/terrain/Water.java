package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.InterpolatedTerrainTriangle;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.GeometricUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 10.04.2016.
 */
public class Water {
    // private Logger logger = Logger.getLogger(Water.class.getName());
    private double level;
    private List<Vertex> vertices = new ArrayList<>();
    private List<Vertex> norms = new ArrayList<>();
    private List<Vertex> tangents = new ArrayList<>();
    private List<Vertex> barycentric = new ArrayList<>();
    private Rectangle2D aabb;

    public Water(double level) {
        this.level = level;
    }

    public double getLevel() {
        return level;
    }

    public void addTriangle(Vertex vertex1, Vertex vertex2, Vertex vertex3) {
        vertices.add(createLevelVertex(vertex1));
        if (vertex1.cross(vertex2, vertex3).getZ() >= 0) {
            vertices.add(createLevelVertex(vertex2));
            vertices.add(createLevelVertex(vertex3));
        } else {
            vertices.add(createLevelVertex(vertex3));
            vertices.add(createLevelVertex(vertex2));
        }
        Vertex norm = new Vertex(1, 0, 0);
        norms.add(norm);
        norms.add(norm);
        norms.add(norm);
        Vertex tangent = new Vertex(0, 0, 1);
        tangents.add(tangent);
        tangents.add(tangent);
        tangents.add(tangent);
        barycentric.add(new Vertex(1, 0, 0));
        barycentric.add(new Vertex(0, 1, 0));
        barycentric.add(new Vertex(0, 0, 1));
    }

    private Vertex createLevelVertex(Vertex input) {
        return new Vertex(input.getX(), input.getY(), level);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Vertex> getNorms() {
        return norms;
    }

    public List<Vertex> getTangents() {
        return tangents;
    }

    public List<Vertex> getBarycentric() {
        return barycentric;
    }

    public Rectangle2D calculateAabb() {
        if (aabb != null) {
            return aabb;
        }
        Polygon2D waterPolygon = new Polygon2D(new ArrayList<>(DecimalPosition.removeSimilarPoints(Vertex.toXY(getVertices()), 1.0)));
        aabb = waterPolygon.toAabb();

        return aabb;
    }

    public InterpolatedTerrainTriangle getInterpolatedVertexData(DecimalPosition absoluteXY) {
        if(!calculateAabb().contains(absoluteXY)) {
            return null;
        }

        return GeometricUtil.getInterpolatedVertexData(absoluteXY, vertices, norms::get, tangents::get, value -> 0);
    }
}
