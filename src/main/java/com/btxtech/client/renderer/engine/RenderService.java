package com.btxtech.client.renderer.engine;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 20.05.2015.
 */
@Singleton
public class RenderService {
    @Inject
    private Instance<Renderer> renderInstance;
    private List<RenderSwitch> renderQueue = new ArrayList<>();
    private boolean wire;
    private Logger logger = Logger.getLogger(RenderService.class.getName());

    public void init() {
        renderQueue.add(new RenderSwitch(renderInstance.select(TerrainSurfaceRenderer.class).get(), renderInstance.select(TerrainSurfaceWireRender.class).get(), wire));
        renderQueue.add(new RenderSwitch(renderInstance.select(TerrainObjectRenderer.class).get(), renderInstance.select(TerrainObjectWireRender.class).get(), wire));
    }

    public void draw() {
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.getRenderer().draw();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "draw failed", t);
            }
        }
    }

    public void fillBuffers() {
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.getRenderer().fillBuffers();
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "fillBuffers failed", t);
            }
        }
    }

    public void showWire(boolean wire) {
        this.wire = wire;
        for (RenderSwitch renderSwitch : renderQueue) {
            try {
                renderSwitch.doSwitch(wire);
            } catch (Throwable t) {
                logger.log(Level.SEVERE, "showWire failed", t);
            }
        }
    }

}
