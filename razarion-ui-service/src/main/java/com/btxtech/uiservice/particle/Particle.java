package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;

/**
 * Created by Beat
 * 02.02.2017.
 */
public class Particle implements Comparable<Particle> {
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
        velocity = MathHelper.random(particleConfig.getVelocity(), particleConfig.getVelocityRandomPart());
        timeToLive = MathHelper.random(particleConfig.getTimeToLive(), particleConfig.getTimeToLiveRandomPart());
    }

    /**
     * @param timeStamp                time stamp
     * @param viewTransformationMatrix view transformation matrix
     * @return true if particle is not dead
     */
    public boolean tick(long timeStamp, double factor, Matrix4 viewTransformationMatrix, NativeMatrixFactory nativeMatrixFactory) {
        if (startTime + timeToLive < timeStamp) {
            return false;
        }
        double progress = (timeStamp - startTime) / (double) timeToLive;
        // velocity = velocity.multiply(0.9);
        if (velocity != null) {
            position = position.add(velocity.multiply(factor));
            if (particleConfig.getAcceleration() != null) {
                velocity = velocity.add(particleConfig.getAcceleration().multiply(factor));
            }
        }
        if (modelMatrices == null) {
            modelMatrices = ModelMatrices.create4Particle(position, setupScale(progress), progress, particleConfig.getParticleXColorRampOffsetIndex(),nativeMatrixFactory);
        } else {
            modelMatrices.updateProgress(progress);
            modelMatrices.updatePositionScale(position, setupScale(progress), progress);
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

    private double setupScale(double progress) {
        if (particleConfig.getParticleGrowFrom() != null && particleConfig.getParticleGrowTo() != null) {
            double scale = progress * (particleConfig.getParticleGrowTo() - particleConfig.getParticleGrowFrom()) + particleConfig.getParticleGrowFrom();
            if (scale > 0) {
                return scale;
            } else {
                throw new IllegalStateException("Particle.setupScale() Scale is negative: " + scale);
            }
        } else {
            return 1;
        }
    }
}
