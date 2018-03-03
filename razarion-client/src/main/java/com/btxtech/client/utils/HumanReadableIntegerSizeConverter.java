package com.btxtech.client.utils;

import com.btxtech.common.DisplayUtils;
import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * 25.12.2016.
 */
public class HumanReadableIntegerSizeConverter implements Converter<Integer, String> {
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
        throw new UnsupportedOperationException("Only to toWidgetValue conversion allowed");
    }

    @Override
    public String toWidgetValue(Integer modelValue) {
        return DisplayUtils.humanReadableSize(modelValue.longValue(), true);
    }
}
