package com.btxtech.client.guielements;

import com.btxtech.client.utils.DisplayUtils;
import com.google.gwt.dom.client.Document;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;

import java.io.IOException;

/**
 * Created by Beat
 * 02.03.2017.
 */
public class CommaDoubleBox extends ValueBox<Double> {
    public CommaDoubleBox() {
        super(Document.get().createTextInputElement(), new Renderer<Double>() {
            @Override
            public String render(Double object) {
                if (object != null) {
                    return Double.toString(object);
                } else {
                    return "";
                }
            }

            @Override
            public void render(Double object, Appendable appendable) throws IOException {
                appendable.append(render(object));
            }
        }, text -> DisplayUtils.parseDouble(text.toString()));
    }
}