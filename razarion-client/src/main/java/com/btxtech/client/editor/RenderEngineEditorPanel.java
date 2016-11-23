package com.btxtech.client.editor;

import com.btxtech.client.editor.renderer.MonitorRenderTask;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 13.08.2016.
 */
@Templated("RenderEngineEditorPanel.html#render-engine-editor-panel")
public class RenderEngineEditorPanel extends LeftSideBarContent {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private RenderService renderService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation normalProjectionTransformation;
    @Inject
    private MonitorRenderTask monitorRenderTask;
    @Inject
    @DataField
    private CheckBox showMonitor;
    @Inject
    @DataField
    private CheckBox showDeepMap;
    @Inject
    @DataField
    private CheckBox wireMode;
    @Inject
    @DataField
    private CheckBox showNorm;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label rendererCount;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("topButton")
    private Button topButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("frontButton")
    private Button frontButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("gameButton")
    private Button gameButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField("dumpPositionButton")
    private Button dumpPositionButton;
    @Inject
    @DataField
    private DoubleBox translateX;
    @Inject
    @DataField
    private DoubleBox translateY;
    @Inject
    @DataField
    private DoubleBox translateZ;
    @Inject
    @DataField
    private DoubleBox rotateXSlider;
    @Inject
    @DataField
    private DoubleBox rotateXBox;
    @Inject
    @DataField
    private DoubleBox rotateZSlider;
    @Inject
    @DataField
    private DoubleBox rotateZBox;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @DataField
    private DoubleBox openingAngleYSlider;
    @Inject
    @DataField
    private DoubleBox openingAngleYBox;

    @PostConstruct
    public void init() {
        showMonitor.setValue(monitorRenderTask.isShown());
        showDeepMap.setValue(monitorRenderTask.isShowDeep());
        // TODO wireMode.setValue(renderService.isWire());
        showNorm.setValue(renderService.isShowNorm());
        displayLightDirectionLabel();
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        openingAngleYBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(normalProjectionTransformation.getFovY())));
        rendererCount.setText(Integer.toString(renderService.getRenderQueueSize()));
    }


    @EventHandler("showMonitor")
    public void showMonitorChanged(ChangeEvent e) {
        if (showMonitor.getValue()) {
            monitorRenderTask.showMonitor();
        } else {
            monitorRenderTask.hideMonitor();
        }
    }

    @EventHandler("showDeepMap")
    public void showDeepMapChanged(ChangeEvent e) {
        monitorRenderTask.setShowDeep(showDeepMap.getValue());
    }

    @EventHandler("wireMode")
    public void wireModeChanged(ChangeEvent e) {
        // TODO renderService.showWire(wireMode.getValue());
    }

    @EventHandler("showNorm")
    public void showNormChanged(ChangeEvent e) {
        renderService.setShowNorm(showNorm.getValue());
    }

    private void displayLightDirectionLabel() {
        Vertex direction = camera.getDirection();
        directionLabel.setText(DisplayUtils.formatVertex(direction));
    }

    @EventHandler("openingAngleYSlider")
    public void openingAngleYSliderChanged(ChangeEvent e) {
        normalProjectionTransformation.setFovY(Math.toRadians(openingAngleYSlider.getValue()));
        openingAngleYBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(normalProjectionTransformation.getFovY())));
    }

    @EventHandler("openingAngleYBox")
    public void openingAngleYBoxChanged(ChangeEvent e) {
        normalProjectionTransformation.setFovY(Math.toRadians(openingAngleYBox.getValue()));
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
    }

    @EventHandler("topButton")
    private void handleTopButtonClick(ClickEvent event) {
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

    @EventHandler("dumpPositionButton")
    private void handleDumpPositionButtonClick(ClickEvent event) {
        camera.testPrint();
    }
}
