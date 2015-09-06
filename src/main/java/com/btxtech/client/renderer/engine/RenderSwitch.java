package com.btxtech.client.renderer.engine;

/**
 * Created by Beat
 * 04.09.2015.
 */
public class RenderSwitch {
    private Renderer current;
    private Renderer normalRenderer;
    private Renderer wireRenderer;

    public RenderSwitch(Renderer normalRenderer, Renderer wireRenderer, boolean wire) {
        this.normalRenderer = normalRenderer;
        this.wireRenderer = wireRenderer;
        setRenderable(wire);
    }

    public Renderer getRenderer() {
        return current;
    }

    public void doSwitch(boolean wire) {
        setRenderable(wire);
        current.fillBuffers();
    }

    private void setRenderable(boolean wire) {
        if (wire) {
            current = wireRenderer;
        } else {
            current = normalRenderer;
        }
    }
}
