package com.btxtech.webglemulator.webgl;

import com.btxtech.shared.primitives.Vertex;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.05.2016.
 */
@Singleton
public class WebGlEmulatorShadow extends AbstractWebGlEmulator {
    private double minZNdc;
    private double maxZNdc;

    @Override
    protected void vertexACallback(Vertex vertex, Vertex ndc) {
        updateMinAndMax(ndc);
    }

    @Override
    protected void vertexBCallback(Vertex vertex, Vertex ndc) {
        updateMinAndMax(ndc);
    }

    @Override
    protected void vertexCCallback(Vertex vertex, Vertex ndc) {
        updateMinAndMax(ndc);
    }

    private void updateMinAndMax(Vertex clip) {
        double value = clip.getZ() * 0.5 + 0.5;
        if (value < minZNdc) {
            minZNdc = value;
        }
        if (value > maxZNdc) {
            maxZNdc = value;
        }
    }

    @Override
    protected void beforeDrawArrays() {
        minZNdc = Double.MAX_VALUE;
        maxZNdc = Double.MIN_VALUE;
    }

    @Override
    protected void afterDrawArrays() {
        System.out.println("SHADOW --------------------------------------------");
        System.out.println("minZNdc: " + minZNdc);
        System.out.println("maxZNdc: " + maxZNdc);
        System.out.println("delta: " + (maxZNdc - minZNdc));
        System.out.println("factor: " + 2.0 / (maxZNdc - minZNdc));
        System.out.println("--------------------------------------------");

    }
}
