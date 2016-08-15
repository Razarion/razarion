package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.utils.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
}
