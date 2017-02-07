package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;

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
    private ParticleConfig particleConfig;
    private Vertex velocity;
    private Vertex position;
    private long startTime;
    private ModelMatrices modelMatrices;
    private double cameraDistance;
    private int timeToLive;

    public Particle(long startTime, Vertex startPosition, ParticleConfig particleConfig) {
        this.startTime = startTime;
        this.position = startPosition;
        this.particleConfig = particleConfig;
        // this.velocity = new Vertex(0, 0, 0);
        if (particleConfig.validSpeed()) {
            setupVelocity();
        }

        // this.velocity = new Vertex(Math.random() * SPEED_XY * 2.0 - SPEED_XY, Math.random() * SPEED_XY * 2.0 - SPEED_XY, SPEED_Z);
        timeToLive = MathHelper.random(particleConfig.getTimeToLive(), particleConfig.getTimeToLiveRandomPart());
        // timeToLive = (int) (TIME_TO_LIVE - TIME_TO_LIVE_DELTA + TIME_TO_LIVE_DELTA * Math.random() * 2.0);
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
        if (velocity != null) {
            position = position.add(velocity.multiply(factor * (1.0 - progress))); // TODO velocity falloff
        }
        if (modelMatrices == null) {
            modelMatrices = new ModelMatrices(Matrix4.createTranslation(position), progress);
        } else {
            modelMatrices.setProgress(progress);
            // TODO performance modelMatrices.getModel().setTranslation(position);
            Matrix4 transformation = Matrix4.createTranslation(position);
            boolean valid = true;
            if (particleConfig.getParticleGrow() != null) {
                double scale = 1 + progress * particleConfig.getParticleGrow();
                if (scale > 0) {
                    transformation = transformation.multiply(Matrix4.createScale(scale));
                } else {
                    valid = false;
                }
            }
            if (valid) {
                modelMatrices.setModel(transformation);
            }
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

    private void setupVelocity() {
        velocity = new Vertex(MathHelper.random(particleConfig.getSpeedX(), particleConfig.getSpeedXRandomPart()),
                MathHelper.random(particleConfig.getSpeedY(), particleConfig.getSpeedYRandomPart()),
                MathHelper.random(particleConfig.getSpeedZ(), particleConfig.getSpeedZRandomPart()));
    }

}
