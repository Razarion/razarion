package com.btxtech.client.editor.renderpanel;

import com.btxtech.client.editor.renderer.MonitorRenderTask;
import com.btxtech.client.editor.sidebar.LeftSideBarContent;
import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.dom.CheckboxInput;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 13.08.2016.
 */
@Templated("RenderEngineEditorPanel.html#render-engine-editor-panel")
public class RenderEngineEditorPanel extends LeftSideBarContent {
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
    private CheckboxInput showMonitor;
    @Inject
    @DataField
    private CheckboxInput showDeepMap;
    @Inject
    @DataField
    private CheckboxInput wireMode;
    @Inject
    @DataField
    private CheckboxInput showNorm;
    @Inject
    @DataField
    private Label rendererCount;
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
    @Inject
    @DataField
    private Label directionLabel;
    @Inject
    @DataField
    private DoubleBox openingAngleYSlider;
    @Inject
    @DataField
    private DoubleBox openingAngleYBox;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<RenderTaskModel, RenderTaskComponent> renderTasks;

    @PostConstruct
    public void init() {
        showMonitor.setChecked(monitorRenderTask.isShown());
        showDeepMap.setChecked(monitorRenderTask.isShowDeep());
        // TODO wireMode.setChecked(renderService.isWire());
        showNorm.setChecked(renderService.isShowNorm());
        displayLightDirectionLabel();
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        openingAngleYBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(normalProjectionTransformation.getFovY())));
        rotateZBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(camera.getRotateZ())));
        rotateZSlider.setValue(Math.toDegrees(camera.getRotateZ()));
        rendererCount.setText(Integer.toString(renderService.getRenderQueueSize()));
        rotateXBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(camera.getRotateX())));
        rotateXSlider.setValue(Math.toDegrees(camera.getRotateX()));
        renderTasks.setValue(renderService.getRenderTasks().stream().map(RenderTaskModel::new).collect(Collectors.toList()));
    }


    @EventHandler("showMonitor")
    public void showMonitorChanged(ChangeEvent e) {
        if (showMonitor.getChecked()) {
            monitorRenderTask.showMonitor();
        } else {
            monitorRenderTask.hideMonitor();
        }
    }

    @EventHandler("showDeepMap")
    public void showDeepMapChanged(ChangeEvent e) {
        monitorRenderTask.setShowDeep(showDeepMap.getChecked());
    }

    @EventHandler("wireMode")
    public void wireModeChanged(ChangeEvent e) {
        // TODO renderService.showWire(wireMode.getChecked());
    }

    @EventHandler("showNorm")
    public void showNormChanged(ChangeEvent e) {
        renderService.setShowNorm(showNorm.getChecked());
    }

    private void displayLightDirectionLabel() {
        Vertex direction = camera.getDirection();
        directionLabel.setText(DisplayUtils.formatVertex(direction));
    }

    @EventHandler("rotateXSlider")
    public void rotateXSliderChanged(ChangeEvent e) {
        camera.setRotateX(Math.toRadians(rotateXSlider.getValue()));
        rotateXBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(camera.getRotateX())));
    }

    @EventHandler("rotateXBox")
    public void rotateXBoxChanged(ChangeEvent e) {
        camera.setRotateX(Math.toRadians(rotateXBox.getValue()));
        rotateXSlider.setValue(Math.toDegrees(camera.getRotateX()));
    }

    @EventHandler("rotateZSlider")
    public void rotateZSliderChanged(ChangeEvent e) {
        camera.setRotateZ(Math.toRadians(rotateZSlider.getValue()));
        rotateZBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(camera.getRotateZ())));
    }

    @EventHandler("rotateZBox")
    public void rotateZBoxChanged(ChangeEvent e) {
        camera.setRotateZ(Math.toRadians(rotateZBox.getValue()));
        rotateZSlider.setValue(Math.toDegrees(camera.getRotateZ()));
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
