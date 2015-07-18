package com.btxtech.client;

import com.btxtech.client.dialogs.Control;
import com.btxtech.client.math3d.TriangleRenderManager;
import com.btxtech.client.terrain.Terrain;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@org.jboss.errai.ioc.client.api.EntryPoint
public class Webgl /*implements EntryPoint*/ {
    public static Webgl instance;
    public static final int TEX_IMAGE_WIDTH = 512;
    public static final int TEX_IMAGE_HEIGHT = 512;
    @Inject
    private Logger logger;
    @Inject
    private GameCanvas viewField;
    @Inject
    private TriangleRenderManager triangleRenderManager;
    @Inject
    private ViewFieldMover viewFieldMover;

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

            triangleRenderManager.createTriangleRenderUnit(Terrain.getInstance().getPlateau(), Terrain.ROCK_2_IMAGE);
            triangleRenderManager.createTriangleRenderUnit(Terrain.getInstance().getPlateauTop(), Terrain.GRASS_IMAGE);
            triangleRenderManager.createTriangleRenderUnit(Terrain.getInstance().getGround(), Terrain.GRASS_IMAGE);

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
        triangleRenderManager.fillBuffers();
    }

}
