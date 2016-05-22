package com.btxtech.client.renderer.engine;

/**
 * Created by Beat
 * 03.09.2015.
 */
public interface Renderer {
    void setId(int id);

    void setupImages();

    void fillBuffers();

    void draw();
}
