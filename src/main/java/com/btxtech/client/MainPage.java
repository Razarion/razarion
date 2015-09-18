package com.btxtech.client;

import com.btxtech.client.renderer.GameCanvas;
import com.btxtech.client.renderer.model.ViewTransformation;
import com.btxtech.client.utils.GradToRadConverter;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
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
    private ViewTransformation viewTransformation;
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
    @Bound
    @DataField("shadowLightX")
    private DoubleBox shadowLightPosX;
    @Inject
    @Bound
    @DataField("shadowLightY")
    private DoubleBox shadowLightPosY;
    @Inject
    @Bound
    @DataField("shadowLightZ")
    private DoubleBox shadowLightPosZ;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateX")
    private DoubleBox shadowLightRotateX;
    @Inject
    @Bound(converter = GradToRadConverter.class)
    @DataField("rotateZ")
    private DoubleBox shadowLightRotateZ;
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
        viewTransformation.setTop();
    }

    @EventHandler("frontButton")
    private void handleFrontButtonClick(ClickEvent event) {
        viewTransformation.setFront();
    }

    @EventHandler("gameButton")
    private void handleGameButtonClick(ClickEvent event) {
        viewTransformation.setGame();
    }

    @EventHandler("customButton")
    private void handleCustomButtonnClick(ClickEvent event) {
        viewTransformation.setCustom();
    }

    @EventHandler("dumpPositionButton")
    private void handleDumpPositionButtonClick(ClickEvent event) {
        viewTransformation.testPrint();
    }

}
