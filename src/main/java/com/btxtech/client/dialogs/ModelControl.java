package com.btxtech.client.dialogs;

import com.btxtech.client.renderer.model.ModelTransformation;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 21.04.2015.
 */
public class ModelControl extends Composite {
    interface ModelControlUiBinder extends UiBinder<Widget, ModelControl> {
    }

    private static ModelControlUiBinder ourUiBinder = GWT.create(ModelControlUiBinder.class);
    @UiField
    InputNumber scaleX;
    @UiField
    InputNumber scaleY;
    @UiField
    InputNumber scaleZ;
    @UiField
    InputNumber translateX;
    @UiField
    InputNumber translateY;
    @UiField
    InputNumber translateZ;

    private ModelTransformation modelTransformation;

    public ModelControl(ModelTransformation modelTransformation) {
        this.modelTransformation = modelTransformation;
        initWidget(ourUiBinder.createAndBindUi(this));
        setFields();
    }

    private void setFields() {
        scaleX.setValue(modelTransformation.getScaleX());
        scaleY.setValue(modelTransformation.getScaleY());
        scaleZ.setValue(modelTransformation.getScaleZ());
        translateX.setValue(modelTransformation.getTranslateX());
        translateY.setValue(modelTransformation.getTranslateY());
        translateZ.setValue(modelTransformation.getTranslateZ());
    }

    @UiHandler("scaleX")
    void onScaleX(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setScaleX(valueChangeEvent.getValue());
    }

    @UiHandler("scaleY")
    void onScaleY(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setScaleX(valueChangeEvent.getValue());
    }

    @UiHandler("scaleZ")
    void onScaleZ(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setScaleX(valueChangeEvent.getValue());
    }

    @UiHandler("translateX")
    void onTranslateX(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setTranslateX(valueChangeEvent.getValue());
    }

    @UiHandler("translateY")
    void onTranslateY(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setTranslateY(valueChangeEvent.getValue());
    }

    @UiHandler("translateZ")
    void onTranslateZ(ValueChangeEvent<Double> valueChangeEvent) {
        modelTransformation.setTranslateZ(valueChangeEvent.getValue());
    }
}