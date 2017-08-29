package com.btxtech.client.utils;

import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * 17.09.2015.
 */
public class BooleanNullConverter implements Converter<Boolean, Boolean> {
    @Override
    public Class<Boolean> getModelType() {
        return Boolean.class;
    }

    @Override
    public Class<Boolean> getComponentType() {
        return Boolean.class;
    }

    @Override
    public Boolean toModelValue(Boolean widgetValue) {
        if (widgetValue != null && widgetValue) {
            return true;
        } else {
            return null;
        }
    }

    @Override
    public Boolean toWidgetValue(Boolean modelValue) {
        if (modelValue != null) {
            return modelValue;
        } else {
            return false;
        }
    }
}
