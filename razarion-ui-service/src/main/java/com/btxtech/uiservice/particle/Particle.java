package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 02.02.2017.
 */
public class Particle implements Comparable<Particle> {
    private static final int TIME_TO_LIVE = 1000;
    private static final double SPEED_XY = 10;
    private static final double SPEED_Z = 20;
    private Vertex velocity;
    private Vertex startPosition;
    private long startTime;
    private ModelMatrices modelMatrices;
    private double cameraDistance;

    public Particle(Vertex startPosition, long startTime) {
        this.startTime = startTime;
        this.startPosition = startPosition;
        velocity = new Vertex(Math.random() * SPEED_XY * 2.0 - SPEED_XY, Math.random() * SPEED_XY * 2.0 - SPEED_XY, SPEED_Z);
    }

    /**
     * @param timeStamp                time stamp
     * @param viewTransformationMatrix view transformation matrix
     * @return true if particle is not dead
     */
    public boolean update(long timeStamp, Matrix4 viewTransformationMatrix) {
        if (startTime + TIME_TO_LIVE < timeStamp) {
            return false;
        }
        int delta = (int) (timeStamp - startTime);
        double factor = delta / 1000.0;
        double progress = delta / (double) TIME_TO_LIVE;
        Vertex position = this.startPosition.add(velocity.multiply(factor));
        if (modelMatrices == null) {
            modelMatrices = new ModelMatrices(Matrix4.createTranslation(position), progress);
        } else {
            modelMatrices.setProgress(progress);
            modelMatrices.getModel().setTranslation(position);
        }
        cameraDistance = viewTransformationMatrix.multiply(position, 1.0).getZ();
        return true;
    }

    public ModelMatrices getModelMatrices() {
        return modelMatrices;
    }

    @Override
    public int compareTo(Particle o) {
        return Double.compare(cameraDistance, o.cameraDistance);
    }

    @Override
    public String toString() {
        return "Particle{" +
                "startPosition=" + startPosition +
                ", startTime=" + startTime +
                '}';
    }
}
