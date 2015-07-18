package com.btxtech.client.dialogs;

import com.btxtech.client.math3d.Color;
import com.btxtech.client.math3d.ColorVertex;
import com.btxtech.client.math3d.ColorVertexBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 16.04.2015.
 */
public class ColorVertexView extends Composite {
    interface ColorVertexViewUiBinder extends UiBinder<Widget, ColorVertexView> {
    }

    public static interface ChangeListener {
        void onMatrixChanged(ColorVertex colorVertex);
    }

    private static ColorVertexViewUiBinder ourUiBinder = GWT.create(ColorVertexViewUiBinder.class);
    @UiField
    InputNumber xField;
    @UiField
    InputNumber yField;
    @UiField
    InputNumber zField;
    @UiField
    InputColor color;

    private ColorVertexBuilder colorVertexBuilder;
    private ChangeListener changeListener = null;

    public ColorVertexView() {
        initWidget(ourUiBinder.createAndBindUi(this));
        colorVertexBuilder = new ColorVertexBuilder(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        initFields();
    }

    private void initFields() {
        xField.setValue(colorVertexBuilder.getX());
        yField.setValue(colorVertexBuilder.getY());
        zField.setValue(colorVertexBuilder.getZ());
        color.setValue(colorVertexBuilder.getColor());
    }

    public void setColorVertex(ColorVertex colorVertex) {
        colorVertexBuilder = new ColorVertexBuilder(colorVertex);
        initFields();
    }

    @UiHandler("xField")
    void onXFieldChanged(ValueChangeEvent<Double> valueChangeEvent) {
        colorVertexBuilder.setX(valueChangeEvent.getValue());
        fireColorVertexChanged();
    }

    @UiHandler("yField")
    void onYFieldChanged(ValueChangeEvent<Double> valueChangeEvent) {
        colorVertexBuilder.setY(valueChangeEvent.getValue());
        fireColorVertexChanged();
    }

    @UiHandler("color")
    void onColorChanged(ValueChangeEvent<Color> valueChangeEvent) {
        colorVertexBuilder.setColor(valueChangeEvent.getValue());
        fireColorVertexChanged();
    }

    @UiHandler("zField")
    void onZFieldChanged(ValueChangeEvent<Double> valueChangeEvent) {
        colorVertexBuilder.setZ(valueChangeEvent.getValue());
        fireColorVertexChanged();
    }

    public void setChangeListener(ChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private void fireColorVertexChanged() {
        if (changeListener != null) {
            changeListener.onMatrixChanged(colorVertexBuilder.toColorVertex());
        }
    }

}