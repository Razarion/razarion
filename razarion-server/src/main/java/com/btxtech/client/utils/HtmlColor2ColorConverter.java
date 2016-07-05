package com.btxtech.client.utils;

import com.btxtech.shared.datatypes.Color;
import org.jboss.errai.databinding.client.api.Converter;

/**
 * Created by Beat
 * 28.05.2016.
 */
public class HtmlColor2ColorConverter implements Converter<Color, String> {
    @Override
    public Color toModelValue(String widgetValue) {
        return Color.fromHtmlColor(widgetValue);
    }

    @Override
    public String toWidgetValue(Color modelValue) {
        return modelValue.toHtmlColor();
    }
}
