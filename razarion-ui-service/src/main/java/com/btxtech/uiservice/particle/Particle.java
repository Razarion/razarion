package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 02.02.2017.
 */
public class Particle {
    private static final Vertex VELOCITY = new Vertex(0, 0, 5);
    private static final int TIME_TO_LIVE = 4000;
    public static final double EDGE_LENGTH = 1;
    public static final double HALF_EDGE = EDGE_LENGTH / 2.0;
    private static final double HALF_HEIGHT = EDGE_LENGTH * Math.sqrt(3.0) / 4.0;
    private Vertex startPosition;
    private long startTime;
    private ModelMatrices modelMatrices;

    public Particle(Vertex startPosition, long startTime) {
        this.startTime = startTime;
        this.startPosition = startPosition;
    }

    public static List<Vertex> calculateVertices() {
        List<Vertex> vertices = new ArrayList<>();
        vertices.add(new Vertex(-HALF_EDGE, 0, -HALF_HEIGHT));
        vertices.add(new Vertex(HALF_EDGE, 0, -HALF_HEIGHT));
        vertices.add(new Vertex(0, 0, HALF_HEIGHT));
        return vertices;
    }

    public ModelMatrices update(long timeStamp) {
        if (startTime + TIME_TO_LIVE < timeStamp) {
            return null;
        }
        int delta = (int) (timeStamp - startTime);
        double factor = delta / 1000.0;
        double progress = 1.0 - delta / (double)TIME_TO_LIVE;
        Vertex position = this.startPosition.add(VELOCITY.multiply(factor));
        if(modelMatrices == null) {
            modelMatrices = new ModelMatrices(Matrix4.createTranslation(position), progress);
        } else {
            modelMatrices.setProgress(progress);
            modelMatrices.getModel().setTranslation(position);
        }
        return modelMatrices;
    }

    @Override
    public String toString() {
        return "Particle{" +
                "startPosition=" + startPosition +
                ", startTime=" + startTime +
                '}';
    }
}
