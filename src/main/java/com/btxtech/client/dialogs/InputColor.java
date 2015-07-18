package com.btxtech.client.dialogs;

import com.btxtech.client.math3d.Color;
import com.google.gwt.dom.client.Element;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueBoxBase;

import java.io.IOException;
import java.text.ParseException;

/**
 * Created by Beat
 * 16.04.2015.
 */
public class InputColor extends ValueBoxBase<Color> {

    public static class ColorRenderer extends AbstractRenderer<Color> {
        @Override
        public String render(Color color) {
            return color.toHtmlColor();
        }
    }

    public static class ColorParser implements Parser<Color> {
        @Override
        public Color parse(CharSequence text) throws ParseException {
            return Color.fromHtmlColor(text.toString());
        }
    }

    public InputColor() {
        super(createInputElement(), new ColorRenderer(), new ColorParser());
    }

    private static Element createInputElement() {
        Element input = DOM.createElement("INPUT");
        input.setAttribute("type", "color");
        return input;
    }

}
