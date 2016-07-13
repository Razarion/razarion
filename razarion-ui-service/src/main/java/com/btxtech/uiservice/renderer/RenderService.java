package com.btxtech.uiservice.renderer;

/**
 * Created by Beat
 * 12.07.2016.
 */
public interface RenderService {

    void setupRenderers();

    void enrollAnimation(int animatedMeshId);

    void disenrollAnimation(int animatedMeshId);

    void render();
}
