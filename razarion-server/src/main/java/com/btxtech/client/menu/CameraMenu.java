package com.btxtech.client.menu;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.client.utils.GradToRadConverter;
import com.btxtech.shared.primitives.Vertex;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.PropertyChangeEvent;
import org.jboss.errai.databinding.client.api.PropertyChangeHandler;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 07.11.2015.
 */
@Templated("CameraMenu.html#menu-camera")
public class CameraMenu extends Composite {
    @Inject
    private Camera camera;
    @Inject
    @AutoBound
    private DataBinder<Camera> cameraDataBinder;
    @Inject
    private ProjectionTransformation normalProjectionTransformation;
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
        cameraDataBinder.setModel(camera);
        cameraDataBinder.addPropertyChangeHandler(new PropertyChangeHandler<Object>() {
            @Override
            public void onPropertyChange(PropertyChangeEvent<Object> event) {
                displayLightDirectionLabel();
            }
        });
        displayLightDirectionLabel();
        openingAngleYSlider.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        openingAngleYBox.setText(DisplayUtils.NUMBER_FORMATTER_X_XX.format(Math.toDegrees(normalProjectionTransformation.getFovY())));
    }

    private void displayLightDirectionLabel() {
        Vertex direction = camera.getDirection();
        directionLabel.setText("Light Direction (" + DisplayUtils.formatVertex(direction) + ")");
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
