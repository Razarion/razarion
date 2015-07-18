package com.btxtech.client.dialogs;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.client.DoubleParser;
import com.google.gwt.text.client.DoubleRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Created by Beat
 * 16.04.2015.
 */
public class InputNumber extends ValueBoxBase<Double> {

    public InputNumber() {
        super(createInputElement(), DoubleRenderer.instance(), DoubleParser.instance());
    }

    private static Element createInputElement() {
        Element input = DOM.createElement("INPUT");
        input.setAttribute("type", "number");
        return input;
    }

    public void setStep(double step) {
        getElement().setAttribute("step", Double.toString(step));
    }

    public void setMin(double min) {
        getElement().setAttribute("min", Double.toString(min));
    }

    public void setMax(double max) {
        getElement().setAttribute("max", Double.toString(max));
    }
}
