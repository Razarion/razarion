package com.btxtech.client.terrain;

import com.btxtech.client.renderer.model.Mesh;
import com.btxtech.game.jsre.client.common.DecimalPosition;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beat
 * 17.10.2015.
 */
public class Plateau {
    private static final int PLANE_TOP_HEIGHT = 80;
    private static final int FRACTIONAL_FACTOR = 30;
    private static final int SLOPE_WIDTH = 4;
    private static final Rectangle INDEX_RECT = new Rectangle(30, 20, 30, 15);
    private Mesh mesh;
    private double slopeTopThreshold = 0.6;
    private double slopeTopThresholdFading = 0.4;
    private double bumpMapDepth = 10;
    private double specularIntensity = 0.5;
    private double specularHardness = 10;
    // private Logger logger = Logger.getLogger(Plateau.class.getName());

    public Plateau(Mesh mesh) {
        this.mesh = mesh;
    }

    public void sculpt() {
        final FractalField fractalField = FractalField.createSaveFractalField(INDEX_RECT.getWidth() + 2 * SLOPE_WIDTH, INDEX_RECT.getHeight() + 2 * SLOPE_WIDTH, FRACTIONAL_FACTOR, -FRACTIONAL_FACTOR, 1.0);

        // Model slope
        final Collection<Index> slopeIndexes = new ArrayList<>();
        mesh.iterate(new Mesh.VertexVisitor() {
            @Override
            public void onVisit(Index index, Vertex vertex) {
                if (INDEX_RECT.contains2(new DecimalPosition(index))) {
                    mesh.setVertex(index, vertex.add(0, 0, PLANE_TOP_HEIGHT));
                } else {
                    double distance = INDEX_RECT.getNearestPointInclusive(new DecimalPosition(index)).getDistance(new DecimalPosition(index));
                    if (distance < SLOPE_WIDTH) {
                        double height = PLANE_TOP_HEIGHT - PLANE_TOP_HEIGHT * distance / (double) SLOPE_WIDTH;
                        mesh.getVertexDataSafe(index).addZValue(height);
                        slopeIndexes.add(index);
                    }
                }
            }
        });

        // Calculate fractal
        Map<Index, Vertex> displacements = new HashMap<>();
        for (Index slopeIndex : slopeIndexes) {
            Vertex norm = mesh.getVertexNormSafe(slopeIndex);
            displacements.put(slopeIndex, norm.multiply(fractalField.get(slopeIndex)));
        }

        // Apply fractal
        for (Map.Entry<Index, Vertex> entry : displacements.entrySet()) {
            mesh.getVertexDataSafe(entry.getKey()).add(entry.getValue());
        }

    }

    public double getBumpMapDepth() {
        return bumpMapDepth;
    }

    public void setBumpMapDepth(double bumpMapDepth) {
        this.bumpMapDepth = bumpMapDepth;
    }

    public double getSpecularHardness() {
        return specularHardness;
    }

    public void setSpecularHardness(double specularHardness) {
        this.specularHardness = specularHardness;
    }

    public double getSpecularIntensity() {
        return specularIntensity;
    }

    public void setSpecularIntensity(double specularIntensity) {
        this.specularIntensity = specularIntensity;
    }

    public double getSlopeTopThreshold() {
        return slopeTopThreshold;
    }

    public void setSlopeTopThreshold(double slopeTopThreshold) {
        this.slopeTopThreshold = slopeTopThreshold;
    }

    public double getSlopeTopThresholdFading() {
        return slopeTopThresholdFading;
    }

    public void setSlopeTopThresholdFading(double slopeTopThresholdFading) {
        this.slopeTopThresholdFading = slopeTopThresholdFading;
    }
}
