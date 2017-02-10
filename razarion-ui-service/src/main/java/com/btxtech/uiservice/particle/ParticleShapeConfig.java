package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 08.02.2017.
 */
public class ParticleShapeConfig {
    private int id;
    private String internalName;
    private double edgeLength;
    private Integer colorRampImageId;
    private Integer alphaOffsetImageId; // rad canal = alpha, greed canal = offset
    private double[] colorRampXOffsets; // 0..1 for x part of the colorramp lookup
    private double textureOffsetScope; // 0 .. 0.5 for scoping the offset change to the colorramp from the green part of the alphaOffsetImage

    public int getId() {
        return id;
    }

    public ParticleShapeConfig setId(int id) {
        this.id = id;
        return this;
    }

    public String getInternalName() {
        return internalName;
    }

    public ParticleShapeConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public double getEdgeLength() {
        return edgeLength;
    }

    public ParticleShapeConfig setEdgeLength(double edgeLength) {
        this.edgeLength = edgeLength;
        return this;
    }

    public Integer getColorRampImageId() {
        return colorRampImageId;
    }

    public ParticleShapeConfig setColorRampImageId(Integer colorRampImageId) {
        this.colorRampImageId = colorRampImageId;
        return this;
    }

    public Integer getAlphaOffsetImageId() {
        return alphaOffsetImageId;
    }

    public ParticleShapeConfig setAlphaOffsetImageId(Integer alphaOffsetImageId) {
        this.alphaOffsetImageId = alphaOffsetImageId;
        return this;
    }

    public double[] getColorRampXOffsets() {
        return colorRampXOffsets;
    }

    public double getColorRampXOffset(int index) {
        return colorRampXOffsets[index];
    }

    public ParticleShapeConfig setColorRampXOffsets(double[] colorRampXOffsets) {
        this.colorRampXOffsets = colorRampXOffsets;
        return this;
    }

    public double getTextureOffsetScope() {
        return textureOffsetScope;
    }

    public ParticleShapeConfig setTextureOffsetScope(double textureOffsetScope) {
        this.textureOffsetScope = textureOffsetScope;
        return this;
    }


    public List<Vertex> calculateVertices(double rotationX) {
        Matrix4 billboardMatrix = Matrix4.createXRotation(rotationX);
        List<Vertex> vertices = new ArrayList<>();
        double halfEdge = edgeLength / 2.0;
        // Triangle 1
        vertices.add(billboardMatrix.multiply(new Vertex(-halfEdge, 0, -halfEdge), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(halfEdge, 0, -halfEdge), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(-halfEdge, 0, halfEdge), 1.0));
        // Triangle 2
        vertices.add(billboardMatrix.multiply(new Vertex(halfEdge, 0, -halfEdge), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(halfEdge, 0, halfEdge), 1.0));
        vertices.add(billboardMatrix.multiply(new Vertex(-halfEdge, 0, halfEdge), 1.0));
        return vertices;
    }

    public List<DecimalPosition> calculateAlphaTextureCoordinates() {
        List<DecimalPosition> alphaTextureCoordinates = new ArrayList<>();
        // Triangle 1
        alphaTextureCoordinates.add(new DecimalPosition(0, 0));
        alphaTextureCoordinates.add(new DecimalPosition(1, 0));
        alphaTextureCoordinates.add(new DecimalPosition(0, 1));
        // Triangle 2
        alphaTextureCoordinates.add(new DecimalPosition(1, 0));
        alphaTextureCoordinates.add(new DecimalPosition(1, 1));
        alphaTextureCoordinates.add(new DecimalPosition(0, 1));
        return alphaTextureCoordinates;
    }

    @Override
    public String toString() {
        return "ParticleShapeConfig{" +
                "id=" + id +
                ", internalName='" + internalName + '\'' +
                '}';
    }
}
