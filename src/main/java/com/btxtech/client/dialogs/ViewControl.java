package com.btxtech.client.dialogs;

import com.btxtech.client.math3d.ViewTransformation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 25.04.2015.
 */
public class ViewControl extends Composite implements ViewTransformation.ViewTransformationObserver {
    interface ViewControlUiBinder extends UiBinder<Widget, ViewControl> {
    }

    private static ViewControlUiBinder ourUiBinder = GWT.create(ViewControlUiBinder.class);
    @UiField
    InputNumber translateX;
    @UiField
    InputNumber translateY;
    @UiField
    InputNumber translateZ;
    @UiField
    VerticalInputRangeNumber rotateX;
    @UiField
    HorizontalInputRangeNumber rotateZ;
    @UiField
    Button topButton;
    @UiField
    Button frontButton;
    @UiField
    Button gameButton;
    private ViewTransformation viewTransformation;

    public ViewControl(ViewTransformation viewTransformation) {
        this.viewTransformation = viewTransformation;
        initWidget(ourUiBinder.createAndBindUi(this));
        updateFields();
        viewTransformation.setViewTransformationObserver(this);
    }

    private void updateFields() {
        translateX.setValue(viewTransformation.getTranslateX());
        translateY.setValue(viewTransformation.getTranslateY());
        translateZ.setValue(viewTransformation.getTranslateZ());
        rotateX.setValue(Math.toDegrees(viewTransformation.getRotateX()));
        rotateZ.setValue(Math.toDegrees(viewTransformation.getRotateZ()));
    }

    @UiHandler("translateX")
    void onXTranslateXChanged(ValueChangeEvent<Double> valueChangeEvent) {
        viewTransformation.setTranslateX(valueChangeEvent.getValue());
    }

    @UiHandler("translateY")
    void onYTranslateXChanged(ValueChangeEvent<Double> valueChangeEvent) {
        viewTransformation.setTranslateY(valueChangeEvent.getValue());
    }

    @UiHandler("translateZ")
    void onZTranslateXChanged(ValueChangeEvent<Double> valueChangeEvent) {
        viewTransformation.setTranslateZ(valueChangeEvent.getValue());
    }

    @UiHandler("rotateX")
    void onRotateXChanged(ValueChangeEvent<Double> valueChangeEvent) {
        viewTransformation.setRotateX(Math.toRadians(valueChangeEvent.getValue()));
    }

    @UiHandler("rotateZ")
    void onRotateZChanged(ValueChangeEvent<Double> valueChangeEvent) {
        viewTransformation.setRotateZ(Math.toRadians(valueChangeEvent.getValue()));
    }

    @UiHandler("topButton")
    void onTopButton(ClickEvent valueChangeEvent) {
        viewTransformation.setTop();
        updateFields();
    }

    @UiHandler("frontButton")
    void onFrontButton(ClickEvent valueChangeEvent) {
        viewTransformation.setFront();
        updateFields();
    }

    @UiHandler("gameButton")
    void onGameButton(ClickEvent valueChangeEvent) {
        viewTransformation.setGame();
        updateFields();
    }

    @Override
    public void onChanged() {
        updateFields();
    }
}