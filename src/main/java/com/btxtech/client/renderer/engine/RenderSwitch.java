package com.btxtech.client.renderer.engine;

/**
 * Created by Beat
 * 04.09.2015.
 */
public class RenderSwitch {
    private Renderer current;
    private Renderer normalRenderer;
    private Renderer depthBufferRenderer;
    private Renderer wireRenderer;

    public RenderSwitch(Renderer normalRenderer, Renderer depthBufferRenderer, Renderer wireRenderer, boolean wire) {
        this.normalRenderer = normalRenderer;
        this.depthBufferRenderer = depthBufferRenderer;
        this.wireRenderer = wireRenderer;
        setRenderable(wire);
    }

    public void draw() {
        if(current != null) {
            current.draw();
        }
    }

    public void drawDepthBuffer() {
        if(depthBufferRenderer != null) {
            depthBufferRenderer.draw();
        }
    }

    public void drawWire() {
        if(wireRenderer != null) {
            wireRenderer.draw();
        }
    }

    public void fillBuffers() {
        if(current != null) {
            current.fillBuffers();
        }
        if(depthBufferRenderer != null) {
            depthBufferRenderer.fillBuffers();
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

    public void updateObjectModelMatrices() {
        if(normalRenderer instanceof AbstractTerrainObjectRenderer) {
            ((AbstractTerrainObjectRenderer)normalRenderer).updateModelMatrices();
        }
        if(depthBufferRenderer instanceof AbstractTerrainObjectDepthBufferRenderer) {
            ((AbstractTerrainObjectDepthBufferRenderer)depthBufferRenderer).updateModelMatrices();
        }
        if(wireRenderer instanceof AbstractTerrainObjectWireRender) {
            ((AbstractTerrainObjectWireRender)wireRenderer).updateModelMatrices();
        }
    }
}
