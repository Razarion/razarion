package com.btxtech.client.editor;

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
import com.google.gwt.user.client.ui.Composite;
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
    @Inject
    private RenderService renderService;
    @Inject
    private Camera camera;
    @Inject
    private ProjectionTransformation normalProjectionTransformation;
    @Inject
    @AutoBound
    private DataBinder<Camera> cameraDataBinder;
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
    @Bound
    @DataField
    private DoubleBox translateX;
    @Inject
    @Bound
    @DataField
    private DoubleBox translateY;
    @Inject
    @Bound
    @DataField
    private DoubleBox translateZ;
    @Inject
    @Bound(property = "rotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateXSlider;
    @Inject
    @Bound(property = "rotateX", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateXBox;
    @Inject
    @Bound(property = "rotateZ", converter = GradToRadConverter.class)
    @DataField
    private DoubleBox rotateZSlider;
    @Inject
    @Bound(property = "rotateZ", converter = GradToRadConverter.class)
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

    @PostConstruct
    public void init() {
        // TODO showMonitor.setValue(renderService.isShowMonitor());
        // TODO showDeepMap.setValue(renderService.isShowDeep());
        // TODO wireMode.setValue(renderService.isWire());
        // TODO showNorm.setValue(renderService.isShowNorm());
        cameraDataBinder.setModel(camera);
        cameraDataBinder.addPropertyChangeHandler(event -> displayLightDirectionLabel());
        displayLightDirectionLabel();
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        openingAngleYBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(normalProjectionTransformation.getFovY())));
        rendererCount.setText(Integer.toString(renderService.getRenderQueueSize()));
    }


    @EventHandler("showMonitor")
    public void showMonitorChanged(ChangeEvent e) {
        // TODO renderService.setShowMonitor(showMonitor.getValue());
    }

    @EventHandler("showDeepMap")
    public void showDeepMapChanged(ChangeEvent e) {
        // TODO renderService.setShowDeep(showDeepMap.getValue());
    }

    @EventHandler("wireMode")
    public void wireModeChanged(ChangeEvent e) {
        // TODO renderService.showWire(wireMode.getValue());
    }

    @EventHandler("showNorm")
    public void showNormChanged(ChangeEvent e) {
        // TODO renderService.setShowNorm(showNorm.getValue());
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
