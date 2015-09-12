package com.btxtech.client.renderer.engine;

/**
 * Created by Beat
 * 04.09.2015.
 */
public class RenderSwitch {
    private Renderer current;
    private Renderer normalRenderer;
    private Renderer wireRenderer;
    private boolean shadow;

    public RenderSwitch(Renderer normalRenderer, Renderer wireRenderer, boolean wire, boolean shadow) {
        this.normalRenderer = normalRenderer;
        this.wireRenderer = wireRenderer;
        this.shadow = shadow;
        setRenderable(wire);
    }

    public void draw() {
        if(current != null) {
            current.draw();
        }
    }

    public void fillBuffers() {
        if(current != null) {
            current.fillBuffers();
        }
    }

    public void doSwitch(boolean wire) {
        setRenderable(wire);
        fillBuffers();
    }

    private void setRenderable(boolean wire) {
        if (wire) {
            current = wireRenderer;
        } else {
            current = normalRenderer;
        }
    }

    public boolean isShadow() {
        return shadow;
    }
}
