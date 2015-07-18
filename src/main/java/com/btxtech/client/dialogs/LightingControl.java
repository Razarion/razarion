package com.btxtech.client.dialogs;

import com.btxtech.client.math3d.Color;
import com.btxtech.client.math3d.Lighting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by Beat
 * 23.06.2015.
 */
public class LightingControl extends Composite {
    private Lighting lighting;

    interface DirectionalLightUiBinder extends UiBinder<Widget, LightingControl> {
    }

    private static DirectionalLightUiBinder ourUiBinder = GWT.create(DirectionalLightUiBinder.class);
    @UiField
    InputColor directionalColor;
    @UiField
    InputColor ambientColor;
    @UiField
    VerticalInputRangeNumber altitude;
    @UiField
    HorizontalInputRangeNumber azimuth;

    public LightingControl(Lighting lighting) {
        this.lighting = lighting;
        initWidget(ourUiBinder.createAndBindUi(this));
        directionalColor.setValue(lighting.getColor());
        azimuth.setValue(Math.toDegrees(lighting.getAzimuth()));
        altitude.setValue(Math.toDegrees(lighting.getAltitude()));
        ambientColor.setValue(lighting.getAmbientColor());
    }

    @UiHandler("directionalColor")
    void onDirectionalColorChanged(ValueChangeEvent<Color> valueChangeEvent) {
        lighting.setColor(valueChangeEvent.getValue());
    }

    @UiHandler("ambientColor")
    void onAmbientColorChanged(ValueChangeEvent<Color> valueChangeEvent) {
        lighting.setAmbientColor(valueChangeEvent.getValue());
    }

    @UiHandler("azimuth")
    void onAzimuthChanged(ValueChangeEvent<Double> valueChangeEvent) {
        lighting.setAzimuth(Math.toRadians(valueChangeEvent.getValue()));
    }

    @UiHandler("altitude")
    void onAltitudeChanged(ValueChangeEvent<Double> valueChangeEvent) {
        lighting.setAltitude(Math.toRadians(valueChangeEvent.getValue()));
    }
}