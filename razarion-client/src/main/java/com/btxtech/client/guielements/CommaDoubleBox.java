package com.btxtech.client.guielements;

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
                return Double.toString(object);
            }

            @Override
            public void render(Double object, Appendable appendable) throws IOException {
                appendable.append(render(object));
            }
        }, text -> Double.parseDouble(text.toString()));
    }
}