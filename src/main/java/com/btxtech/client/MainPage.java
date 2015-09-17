package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.client.utils.GradToRadConverter;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Model;
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
    private Shadowing shadowing;
    @Inject
    @AutoBound
    // Unfortunately Shadowing is instatiated here. The correct Shadowing is set in the @PostConstruct method
    private DataBinder<Shadowing> dataBinder;
    @Inject
    @DataField("surfaceSlider")
    private DoubleBox surfaceSlider;
    @Inject
    @Bound
    @DataField("shadowLightX")
    private DoubleBox x;
    @Inject
    @Bound
    @DataField("shadowLightY")
    private DoubleBox y;
    @Inject
    @Bound
    @DataField("shadowLightZ")
    private DoubleBox z;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateX")
    private DoubleBox rotateX;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateZ")
    private DoubleBox rotateZ;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            gameCanvas.init(canvas);
            dataBinder.setModel(shadowing);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
    }

    @EventHandler("surfaceSlider")
    public void onSurfaceSliderChanged(ChangeEvent e) {
        terrainSurface.setEdgeDistance(surfaceSlider.getValue());

    }
}
