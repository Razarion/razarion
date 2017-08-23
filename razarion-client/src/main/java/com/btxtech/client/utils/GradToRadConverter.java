package com.btxtech.client.utils;

import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * 17.09.2015.
 */
public class GradToRadConverter implements Converter<Double, Double> {
    @Override
    public Class<Double> getModelType() {
        return Double.class;
    }

    @Override
    public Class<Double> getComponentType() {
        return Double.class;
    }

    @Override
    public Double toModelValue(Double widgetValue) {
        if (widgetValue != null) {
            return Math.toRadians(widgetValue);
        } else {
            return null;
        }
    }

    @Override
    public Double toWidgetValue(Double modelValue) {
        if (modelValue != null) {
            return Math.toDegrees(modelValue);
        } else {
            return null;
        }
    }
}
