package com.btxtech.uiservice.particle;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Beat
 * 02.02.2017.
 */
@ApplicationScoped
public class ParticleService {
    private static final int GENERATION_DELAY = 100;
    private static final int GENERATION_COUNT = 50;
    private static final int GENERATION_RANDOM_DISTANCE = 5;
    private List<Particle> particles = new ArrayList<>();
    private long lastGenerationTime;

    public List<ModelMatrices> provideModelMatrices(long timestamp) {
        List<ModelMatrices> modelMatricesList = new ArrayList<>();
        if (lastGenerationTime + GENERATION_DELAY < timestamp) {
            for (int i = 0; i < GENERATION_COUNT; i++) {
                double xRand = Math.random() * GENERATION_RANDOM_DISTANCE;
                double yRand = Math.random() * GENERATION_RANDOM_DISTANCE;
                particles.add(new Particle(new Vertex(200 + xRand, 200 + yRand, 2), timestamp));
            }
            lastGenerationTime = timestamp;
        }
        for (Iterator<Particle> iterator = particles.iterator(); iterator.hasNext(); ) {
            Particle particle = iterator.next();
            ModelMatrices modelMatrices = particle.update(timestamp);
            if (modelMatrices != null) {
                modelMatricesList.add(modelMatrices);
            } else {
                iterator.remove();
            }
        }
        return modelMatricesList;
    }

}
