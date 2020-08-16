package com.btxtech.client.editor.renderpanel;

import com.btxtech.client.editor.editorpanel.AbstractEditor;
import com.btxtech.client.editor.renderer.MonitorRenderTask;
import com.btxtech.client.guielements.CommaDoubleBox;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.renderer.RenderService;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import elemental2.dom.HTMLInputElement;
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
public class RenderEngineEditorPanel extends AbstractEditor {
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
    @DataField("updateButton")
    private Button updateButton;
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
    private CommaDoubleBox translateX;
    @Inject
    @DataField
    private CommaDoubleBox translateY;
    @Inject
    @DataField
    private CommaDoubleBox translateZ;
    @Inject
    @DataField
    private DoubleBox rotateXSlider;
    @Inject
    @DataField
    private HTMLInputElement rotateXBox;
    @Inject
    @DataField
    private DoubleBox rotateZSlider;
    @Inject
    @DataField
    private HTMLInputElement rotateZBox;
    @Inject
    @DataField
    private DoubleBox openingAngleYSlider;
    @Inject
    @DataField
    private HTMLInputElement openingAngleYBox;
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
        rendererCount.setText(Integer.toString(renderService.getRenderQueueSize()));
        renderTasks.setValue(renderService.getRenderTasks().stream().map(RenderTaskModel::new).collect(Collectors.toList()));
        updateCamera();
    }

    private void updateCamera() {
        translateX.setValue(camera.getTranslateX());
        translateY.setValue(camera.getTranslateY());
        translateZ.setValue(camera.getTranslateZ());
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        openingAngleYBox.value = Double.toString(Math.toDegrees(normalProjectionTransformation.getFovY()));
        rotateZBox.value = Double.toString(Math.toDegrees(camera.getRotateZ()));
        rotateZSlider.setValue(Math.toDegrees(camera.getRotateZ()));
        rotateXBox.value = Double.toString(Math.toDegrees(camera.getRotateX()));
        rotateXSlider.setValue(Math.toDegrees(camera.getRotateX()));
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

    @EventHandler("translateX")
    public void translateXBoxChanged(ChangeEvent e) {
        camera.setTranslateX(translateX.getValue());
    }

    @EventHandler("translateY")
    public void translateYBoxChanged(ChangeEvent e) {
        camera.setTranslateY(translateY.getValue());
    }

    @EventHandler("translateZ")
    public void translateZBoxChanged(ChangeEvent e) {
        camera.setTranslateZ(translateZ.getValue());
    }

    @EventHandler("rotateXSlider")
    public void rotateXSliderChanged(ChangeEvent e) {
        camera.setRotateX(Math.toRadians(rotateXSlider.getValue()));
        rotateXBox.value = Double.toString(Math.toDegrees(camera.getRotateX()));
    }

    @EventHandler("rotateXBox")
    public void rotateXBoxChanged(ChangeEvent e) {
        camera.setRotateX(Math.toRadians(rotateXBox.valueAsNumber));
        rotateXSlider.setValue(Math.toDegrees(camera.getRotateX()));
    }

    @EventHandler("rotateZSlider")
    public void rotateZSliderChanged(ChangeEvent e) {
        camera.setRotateZ(Math.toRadians(rotateZSlider.getValue()));
        rotateZBox.value = Double.toString(Math.toDegrees(camera.getRotateZ()));
    }

    @EventHandler("rotateZBox")
    public void rotateZBoxChanged(ChangeEvent e) {
        camera.setRotateZ(Math.toRadians(rotateZBox.valueAsNumber));
        rotateZSlider.setValue(Math.toDegrees(camera.getRotateZ()));
    }

    @EventHandler("openingAngleYSlider")
    public void openingAngleYSliderChanged(ChangeEvent e) {
        normalProjectionTransformation.setFovY(Math.toRadians(openingAngleYSlider.getValue()));
        openingAngleYBox.value = Double.toString(Math.toDegrees(normalProjectionTransformation.getFovY()));
    }

    @EventHandler("openingAngleYBox")
    public void openingAngleYBoxChanged(ChangeEvent e) {
        normalProjectionTransformation.setFovY(Math.toRadians(openingAngleYBox.valueAsNumber));
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
    }

    @EventHandler("updateButton")
    private void handleUpdateButtonClick(ClickEvent event) {
        updateCamera();
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
