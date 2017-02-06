package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

/**
 * Created by Beat
 * 02.02.2017.
 */
public class Particle implements Comparable<Particle> {
    private static final int TIME_TO_LIVE = 2000;
    private static final int TIME_TO_LIVE_DELTA = 1000;
    private static final double SPEED_XY = 5;
    private static final double SPEED_Z = 3;
    private static final double PARTICLE_GROW = 1.5;
    private Vertex velocity;
    private Vertex position;
    private long startTime;
    private long lastTime;
    private ModelMatrices modelMatrices;
    private double cameraDistance;
    private int timeToLive;

    public Particle(long startTime, Vertex startPosition, Vertex velocity) {
        this.startTime = startTime;
        this.position = startPosition;
        // this.velocity = new Vertex(0, 0, 0);
        this.velocity = new Vertex(Math.random() * SPEED_XY * 2.0 - SPEED_XY, Math.random() * SPEED_XY * 2.0 - SPEED_XY, SPEED_Z);
        timeToLive = (int) (TIME_TO_LIVE - TIME_TO_LIVE_DELTA  + TIME_TO_LIVE_DELTA * Math.random() * 2.0);
        // timeToLive = TIME_TO_LIVE;
        // this.velocity = velocity;
    }

    /**
     * @param timeStamp                time stamp
     * @param viewTransformationMatrix view transformation matrix
     * @return true if particle is not dead
     */
    public boolean tick(long timeStamp, double factor, Matrix4 viewTransformationMatrix) {
        if (startTime + timeToLive < timeStamp) {
            return false;
        }
        double progress = (timeStamp - startTime) / (double) timeToLive;
        // velocity = velocity.multiply(0.9);
        position = position.add(velocity.multiply(factor * (1.0 - progress)));
        if (modelMatrices == null) {
            modelMatrices = new ModelMatrices(Matrix4.createTranslation(position), progress);
        } else {
            modelMatrices.setProgress(progress);
            // TODO modelMatrices.getModel().setTranslation(position);
            modelMatrices.setModel(Matrix4.createTranslation(position).multiply(Matrix4.createScale(1 + progress * PARTICLE_GROW)));
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
}
