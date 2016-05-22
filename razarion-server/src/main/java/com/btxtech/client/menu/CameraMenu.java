package com.btxtech.client.menu;

import com.btxtech.client.renderer.model.Camera;
import com.btxtech.client.renderer.model.ProjectionTransformation;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Label;
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
    @DataField
    private DoubleBox viewTransformationX;
    @Inject
    @DataField
    private DoubleBox viewTransformationY;
    @Inject
    @DataField
    private DoubleBox viewTransformationZ;
    @Inject
    @DataField
    private Label viewTransformationRotateXDisplay;
    @Inject
    @DataField
    private DoubleBox viewTransformationRotateX;
    @Inject
    @DataField
    private Label viewTransformationRotateZDisplay;
    @Inject
    @DataField
    private DoubleBox viewTransformationRotateZ;
    @Inject
    @DataField
    private Label projectionTransformationZoomDisplay;
    @Inject
    @DataField
    private DoubleBox projectionTransformationZoom;

    @PostConstruct
    public void init() {
        viewTransformationX.setValue(camera.getTranslateX());
        viewTransformationY.setValue(camera.getTranslateY());
        viewTransformationZ.setValue(camera.getTranslateZ());
        viewTransformationRotateX.setValue(Math.toDegrees(camera.getRotateX()));
        viewTransformationRotateXDisplay.setText(Double.toString(Math.toDegrees(camera.getRotateX())));
        viewTransformationRotateZ.setValue(Math.toDegrees(camera.getRotateZ()));
        viewTransformationRotateZDisplay.setText(Double.toString(Math.toDegrees(camera.getRotateZ())));
        projectionTransformationZoom.setValue(Math.toDegrees(normalProjectionTransformation.getFovY()));
        projectionTransformationZoomDisplay.setText(Double.toString(Math.toDegrees(normalProjectionTransformation.getFovY())));
    }

    @EventHandler("viewTransformationRotateX")
    public void viewTransformationRotateXChanged(ChangeEvent e) {
        camera.setRotateX(Math.toRadians(viewTransformationRotateX.getValue()));
        viewTransformationRotateXDisplay.setText(Double.toString(Math.toDegrees(camera.getRotateX())));
    }

    @EventHandler("viewTransformationRotateZ")
    public void viewTransformationRotateZChanged(ChangeEvent e) {
        camera.setRotateZ(Math.toRadians(viewTransformationRotateZ.getValue()));
        viewTransformationRotateZDisplay.setText(Double.toString(Math.toDegrees(camera.getRotateZ())));
    }

    @EventHandler("projectionTransformationZoom")
    public void projectionTransformationZoomChanged(ChangeEvent e) {
        normalProjectionTransformation.setFovY(Math.toRadians(projectionTransformationZoom.getValue()));
        projectionTransformationZoomDisplay.setText(Double.toString(Math.toDegrees(normalProjectionTransformation.getFovY())));
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
