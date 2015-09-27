package com.btxtech.client.renderer.model;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 26.09.2015.
 */
@Singleton
@Shadow
public class ShadowProjectionTransformation extends AbstractProjectionTransformation implements ProjectionTransformation {
    public ShadowProjectionTransformation() {
        setFovY(Math.toRadians(111));
        setAspectRatio(1.0);
        setZNear(150);
        setZFar(201);
    }
}
