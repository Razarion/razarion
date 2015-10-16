package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.Shadowing;
import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.client.utils.RadToStringGradConverter;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
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
    private Camera camera;
    @Inject
    private Shadowing shadowing;
    @Inject
    @AutoBound
    private DataBinder<MenuModel> dataBinder;
    @Inject
    private MenuModel menuModel;
    @Inject
    @Bound
    @DataField("surfaceSlider")
    private DoubleBox surfaceSlider;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateX")
    private DoubleBox shadowLightRotateX;
    @Inject
    @Bound(property = "shadowLightRotateX", converter = RadToStringGradConverter.class)
    @DataField("rotateXDisplay")
    private Label rotateXDisplay;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateY")
    private DoubleBox shadowLightRotateY;
    @Inject
    @Bound(property = "shadowLightRotateY", converter = RadToStringGradConverter.class)
    @DataField("rotateYDisplay")
    private Label rotateYDisplay;
    @Inject
    @Bound
    @DataField("shadowProjectionTransformationZNear")
    private DoubleBox shadowProjectionTransformationZNear;
    @Inject
    @DataField("topButton")
    private Button topButton;
    @Inject
    @DataField("frontButton")
    private Button frontButton;
    @Inject
    @DataField("gameButton")
    private Button gameButton;
    @Inject
    @DataField("customButton")
    private Button customButton;
    @Inject
    @DataField("dumpPositionButton")
    private Button dumpPositionButton;
    @Inject
    @Bound
    @DataField("shadowAlpha")
    private DoubleBox shadowAlpha;
    @Inject
    @Bound
    @DataField("bumpMapDepth")
    private DoubleBox bumpMapDepth;
    @Inject
    @DataField("dumpShadowPositionButton")
    private Button dumpShadowPositionButton;
    @Inject
    @Bound
    @DataField("viewTransformationX")
    private DoubleBox viewTransformationX;
    @Inject
    @Bound
    @DataField("viewTransformationY")
    private DoubleBox viewTransformationY;
    @Inject
    @Bound
    @DataField("viewTransformationZ")
    private DoubleBox viewTransformationZ;
    @Inject
    @Bound(property = "viewTransformationRotateX", converter = RadToStringGradConverter.class)
    @DataField("viewTransformationRotateXDisplay")
    private Label viewTransformationRotateXDisplay;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("viewTransformationRotateX")
    private DoubleBox viewTransformationRotateX;
    @Inject
    @Bound(property = "viewTransformationRotateZ", converter = RadToStringGradConverter.class)
    @DataField("viewTransformationRotateZDisplay")
    private Label viewTransformationRotateZDisplay;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("viewTransformationRotateZ")
    private DoubleBox viewTransformationRotateZ;
    @Inject
    @Bound(property = "projectionTransformationZoom", converter = RadToStringGradConverter.class)
    @DataField("projectionTransformationZoomDisplay")
    private Label projectionTransformationZoomDisplay;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("projectionTransformationZoom")
    private DoubleBox projectionTransformationZoom;
    @Inject
    @Bound
    @DataField("showMonitor")
    private CheckBox showMonitor;
    @Inject
    @Bound
    @DataField("showDeepMap")
    private CheckBox showDeepMap;
    @Inject
    @Bound
    @DataField("wireMode")
    private CheckBox wireMode;

    @PostConstruct
    public void init() {
        try {
            if (canvas == null) {
                throw new IllegalStateException("Canvas is not supported");
            }
            gameCanvas.init(canvas);
            dataBinder.setModel(menuModel);
        } catch (Throwable throwable) {
            logger.log(Level.SEVERE, "MainPage init failed", throwable);
        }
    }

    @EventHandler("topButton")
    private void handleTtopButtonClick(ClickEvent event) {
        camera.setTop();
    }

    @EventHandler("frontButton")
    private void handleFrontButtonClick(ClickEvent event) {
        camera.setFront();
    }

    @EventHandler("gameButton")
    private void handleGameButtonClick(ClickEvent event) {
        camera.setGame();
    }

    @EventHandler("customButton")
    private void handleCustomButtonnClick(ClickEvent event) {
        camera.setCustom();
    }

    @EventHandler("dumpPositionButton")
    private void handleDumpPositionButtonClick(ClickEvent event) {
        camera.testPrint();
    }

    @EventHandler("dumpShadowPositionButton")
    private void handleDumpShadowPositionButtonnClick(ClickEvent event) {
        shadowing.testPrint();
    }

}
