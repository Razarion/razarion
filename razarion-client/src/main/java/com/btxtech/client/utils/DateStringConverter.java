package com.btxtech.client.utils;

import com.btxtech.common.DisplayUtils;
import org.jboss.errai.databinding.client.api.Converter;

import java.util.Date;

/**
 * Created by Beat
 * 17.09.2015.
 */
public class DateStringConverter implements Converter<Date, String> {
    @Override
    public Class<Date> getModelType() {
        return Date.class;
    }

    @Override
    public Class<String> getComponentType() {
        return String.class;
    }

    @Override
    public Date toModelValue(String widgetValue) {
        if(widgetValue == null) {
            return null;
        }
        return DisplayUtils.toDateOnly(widgetValue);
    }

    @Override
    public String toWidgetValue(Date modelValue) {
        if(modelValue == null) {
            return null;
        }
        return DisplayUtils.formatDateOnly(modelValue);
    }
}
