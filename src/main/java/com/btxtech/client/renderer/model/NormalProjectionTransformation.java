package com.btxtech.client.renderer.model;

import javax.inject.Singleton;

/**
 * Created by Beat
 * 26.09.2015.
 */
@Singleton
@Normal
public class NormalProjectionTransformation extends AbstractProjectionTransformation {
    public NormalProjectionTransformation() {
        setFovY(Math.toRadians(45));
        setZNear(300);
        setZFar(1200);
    }
}
