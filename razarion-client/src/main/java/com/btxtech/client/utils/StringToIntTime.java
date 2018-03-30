package com.btxtech.client.utils;

import com.btxtech.common.DisplayUtils;
import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * on 30.03.2018.
 */
public class StringToIntTime implements Converter<Integer, String> {
    @Override
    public Class<Integer> getModelType() {
        return Integer.class;
    }

    @Override
    public Class<String> getComponentType() {
        return String.class;
    }

    @Override
    public Integer toModelValue(String componentValue) {
        if (componentValue == null) {
            return null;
        }
        return DisplayUtils.parsDateMillis(componentValue);
    }

    @Override
    public String toWidgetValue(Integer modelValue) {
        if (modelValue == null) {
            return null;
        }
        return DisplayUtils.formatDateMillis(modelValue);
    }
}
