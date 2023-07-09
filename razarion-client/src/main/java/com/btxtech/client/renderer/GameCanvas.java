package com.btxtech.client.renderer;

import com.btxtech.shared.datatypes.Index;
import elemental2.webgl.WebGLRenderingContext;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 12.07.2015.
 */
@Deprecated
@ApplicationScoped
public class GameCanvas {
    // @Inject does not work
    private double width;
    private double height;

    public void init() {
    }

    public void stopRenderLoop() {
    }

    public WebGLRenderingContext getCtx3d() {
        return null;
    }

    public int getWidth() {
        return (int) width;
    }

    public int getHeight() {
        return (int) height;
    }

    public void enterPlaybackMode() {
    }

    public void setPlaybackDimension(Index browserWindowDimension) {
        width = browserWindowDimension.getX();
        height = browserWindowDimension.getY();
    }

    public Index getWindowDimenionForPlayback() {
        return new Index(getWidth(), getHeight());
    }

    public void setCursor(String cursor) {
    }
}
