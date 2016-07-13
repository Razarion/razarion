package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 12.07.2016.
 */
@Portable
public class AnimatedMeshConfig {
    private VertexContainer vertexContainer;
    private Vertex position;
    private int duration;
    private double scaleFrom;
    private double scaleTo;
    private int textureId;

    public VertexContainer getVertexContainer() {
        return vertexContainer;
    }

    public void setVertexContainer(VertexContainer vertexContainer) {
        this.vertexContainer = vertexContainer;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTextureId() {
        return textureId;
    }

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public double getScaleFrom() {
        return scaleFrom;
    }

    public void setScaleFrom(double scaleFrom) {
        this.scaleFrom = scaleFrom;
    }

    public double getScaleTo() {
        return scaleTo;
    }

    public void setScaleTo(double scaleTo) {
        this.scaleTo = scaleTo;
    }
}
