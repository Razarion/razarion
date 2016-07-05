package com.btxtech.uiservice.terrain;

import com.btxtech.uiservice.ImageDescriptor;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 10.04.2016.
 */
public class Water {
    private Logger logger = Logger.getLogger(Water.class.getName());
    private double level;
    private double ground;
    // Water, should not be in here
    private double waterTransparency = 0.65;
    private double waterBumpMapDepth = 10;
    private List<Vertex> vertices = new ArrayList<>();
    private List<Vertex> norms = new ArrayList<>();
    private List<Vertex> tangents = new ArrayList<>();
    private List<Vertex> barycentric = new ArrayList<>();
    private LightConfig lightConfig;

    public Water(double level, double ground) {
        this.level = level;
        this.ground = ground;
        try {
            lightConfig = new LightConfig();
            lightConfig.setDiffuse(new Color(1, 1, 1));
            lightConfig.setAmbient(new Color(1, 1, 1));
            lightConfig.setXRotation(Math.toRadians(-20));
            lightConfig.setYRotation(Math.toRadians(-20));
            lightConfig.setSpecularIntensity(1.0);
            lightConfig.setSpecularHardness(0.5);
        } catch (Throwable t) {
            logger.log(Level.SEVERE, t.getMessage(), t);
        }
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public double getGround() {
        return ground;
    }

    public void setGround(double ground) {
        this.ground = ground;
    }

    public ImageDescriptor getBumpMap() {
        return ImageDescriptor.BUMP_MAP_01;
    }

    public double getWaterTransparency() {
        return waterTransparency;
    }

    public void setWaterTransparency(double waterTransparency) {
        this.waterTransparency = waterTransparency;
    }

    public double getWaterBumpMapDepth() {
        return waterBumpMapDepth;
    }

    public void setWaterBumpMapDepth(double waterBumpMapDepth) {
        this.waterBumpMapDepth = waterBumpMapDepth;
    }

    public void clearAllTriangles() {
        vertices.clear();
        norms.clear();
        tangents.clear();
        barycentric.clear();
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

    public double getWaterAnimation() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 0);
    }

    public double getWaterAnimation2() {
        return getWaterAnimation(System.currentTimeMillis(), 2000, 500);
    }

    public double getWaterAnimation(long millis, int durationMs, int offsetMs) {
        return Math.sin(((millis % durationMs) / (double) durationMs + ((double) offsetMs / (double) durationMs)) * MathHelper.ONE_RADIANT);
    }

    public LightConfig getLightConfig() {
        return lightConfig;
    }
}
