package com.btxtech.client.dialogs;

import com.btxtech.client.math3d.ProjectionTransformation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 19.04.2015.
 */
public class ProjectionControl extends Composite {
    private ProjectionTransformation projectionTransformation;

    interface ProjectionControlUiBinder extends UiBinder<Widget, ProjectionControl> {
    }

    private static ProjectionControlUiBinder ourUiBinder = GWT.create(ProjectionControlUiBinder.class);
    @UiField
    InputNumber fovY;
    @UiField
    InputNumber aspectRatio;
    @UiField
    InputNumber zNear;
    @UiField
    InputNumber zFar;

    public ProjectionControl(ProjectionTransformation projectionTransformation) {
        this.projectionTransformation = projectionTransformation;
        initWidget(ourUiBinder.createAndBindUi(this));
        setFields();
    }

    private void setFields() {
        fovY.setValue(Math.toDegrees(projectionTransformation.getFovY()));
        aspectRatio.setValue(projectionTransformation.getAspectRatio());
        zNear.setValue(projectionTransformation.getZNear());
        zFar.setValue(projectionTransformation.getZFar());
    }

    @UiHandler("fovY")
    void onFovYChanged(ValueChangeEvent<Double> valueChangeEvent) {
        projectionTransformation.setFovY(Math.toRadians(valueChangeEvent.getValue()));
    }

    @UiHandler("aspectRatio")
    void onAspectRatioChanged(ValueChangeEvent<Double> valueChangeEvent) {
        projectionTransformation.setAspectRatio(valueChangeEvent.getValue());
    }

    @UiHandler("zNear")
    void onZNearChanged(ValueChangeEvent<Double> valueChangeEvent) {
        projectionTransformation.setZNear(valueChangeEvent.getValue());
    }

    @UiHandler("zFar")
    void onZFarChanged(ValueChangeEvent<Double> valueChangeEvent) {
        projectionTransformation.setZFar(valueChangeEvent.getValue());
    }

}