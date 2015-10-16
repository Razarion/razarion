package com.btxtech.client.renderer.model;

import com.btxtech.shared.primitives.Color;
import com.btxtech.shared.primitives.Vertex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 23.06.2015.
 */
@Singleton
public class Lighting {
    private Color ambientColor;
    private Color color;
    @Inject
    private Shadowing shadowing;
    private double bumpMapDepth = 10;
    // private Logger logger = Logger.getLogger(Lighting.class.getName());

    public Lighting() {
        setGame();
    }

    public Color getColor() {
        return new Color(0.2, 0.2, 0.2);
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Color ambientColor) {
        this.ambientColor = ambientColor;
    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    /**
     * Return the point on the surface pointing to the sun
     *
     * @return direction normalized
     */
    public Vertex getLightDirection() {
        return shadowing.createRotationMatrix().multiply(new Vertex(0, 0, 1), 1.0);
    }

    public void setGame() {
        color = new Color(0.6, 0.6, 0.6);
        ambientColor = new Color(0.5, 0.5, 0.5);
    }
}
