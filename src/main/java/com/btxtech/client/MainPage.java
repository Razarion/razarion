package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.terrain.TerrainSurface;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 31.08.2015.
 */
@Page(role = DefaultPage.class)
@Templated("MainPage.html#app-template")
public class MainPage extends Composite {
    private Logger logger = Logger.getLogger(MainPage.class.getName());
    @DataField
    private Canvas canvas = Canvas.createIfSupported();
    @Inject
    private GameCanvas gameCanvas;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    @DataField("surfaceSlider")
    private DoubleBox surfaceSlider;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            gameCanvas.init(canvas);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
    }

    @EventHandler("surfaceSlider")
    public void onSurfaceSliderChanged(ChangeEvent e) {
        terrainSurface.setEdgeDistance(surfaceSlider.getValue());

    }
}
