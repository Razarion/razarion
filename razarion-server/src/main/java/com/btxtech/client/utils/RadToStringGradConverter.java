package com.btxtech.client.utils;

import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * 17.09.2015.
 */
public class RadToStringGradConverter implements Converter<Double, String> {
    @Override
    public Class<Double> getModelType() {
        return Double.class;
    }

    @Override
    public Class<String> getComponentType() {
        return String.class;
    }

    @Override
    public Double toModelValue(String widgetValue) {
        return Math.toRadians(Double.parseDouble(widgetValue));
    }

    @Override
    public String toWidgetValue(Double modelValue) {
        return Double.toString(Math.toDegrees(modelValue));
    }
}
