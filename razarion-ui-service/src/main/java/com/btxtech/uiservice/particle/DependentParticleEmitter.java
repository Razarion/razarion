package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.Dependent;

/**
 * Created by Beat
 * 06.02.2017.
 */
@Dependent
public class DependentParticleEmitter extends ParticleEmitter{
    @Override
    protected boolean isRunning(long timestamp) {
        return true;
    }

    @Override
    protected Vertex updatePosition(long timestamp, Vertex position) {
        return position;
    }
}
