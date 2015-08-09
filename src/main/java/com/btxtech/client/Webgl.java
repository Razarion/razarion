package com.btxtech.client;

import com.btxtech.client.dialogs.Control;
import com.btxtech.client.math3d.TriangleRenderManager;
import com.btxtech.client.terrain.SimpleTerrain;
import com.btxtech.client.terrain.Terrain;
import com.btxtech.client.terrain.Terrain2;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.ioc.client.api.EntryPoint;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@EntryPoint
public class Webgl /*implements EntryPoint*/ {
    public static Webgl instance;
    @Inject
    private Logger logger;
    @Inject
    private GameCanvas viewField;
    @Inject
    private TriangleRenderManager triangleRenderManager;
    @Inject
    private ViewFieldMover viewFieldMover;
    @Inject
    private Terrain2 terrain;
    @Inject
    private SimpleTerrain simpleTerrain;

    @PostConstruct
    public void init() {
        instance = this;
        try {
            GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
                @Override
                public void onUncaughtException(Throwable e) {
                    logger.log(Level.SEVERE, "UncaughtExceptionHandler", e);
                }
            });

            HorizontalPanel horizontalPanel = new HorizontalPanel();

            triangleRenderManager.createTriangleRenderUnit(terrain.getPlainProvider(), Terrain.GRASS_IMAGE);
            triangleRenderManager.createTriangleRenderUnit(terrain.getSlopeProvider(), Terrain.SAND_1);
            // triangleRenderManager.createTriangleRenderUnit(simpleTerrain.getPlainProvider(), Terrain.GRASS_IMAGE);
            // triangleRenderManager.createTriangleRenderUnit(simpleTerrain.getSlopeProvider(), Terrain.SAND_1);

            Control control = new Control(triangleRenderManager);
            horizontalPanel.add(viewField.getCanvas());
            horizontalPanel.add(control);
            RootPanel.get().add(horizontalPanel);
            RootPanel.get().getElement().getStyle().setProperty("userSelect", "none");
            triangleRenderManager.fillBuffers();
            viewField.startRenderLoop(new GameCanvas.RenderCallback() {
                @Override
                public void doRender() {
                    triangleRenderManager.draw();
                }
            });
            viewFieldMover.activate();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "WebGl.onModuleLoad()", t);
        }
    }

    public void fillBuffers() {
        terrain.setupTerrain();
        triangleRenderManager.fillBuffers();
    }

}
