package com.btxtech.client.dialogs;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.client.DoubleParser;
import com.google.gwt.text.client.DoubleRenderer;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.ValueBoxBase;

/**
 * Created by Beat
 * 06.04.2015.
 */
public class InputRange extends ValueBoxBase<Double> {
    public InputRange() {
        super(createInputElement(), DoubleRenderer.instance(), DoubleParser.instance());
    }

    public InputRange(double min, double max, double step, double value, boolean vertical, ValueChangeHandler<Double> valueChangeHandler) {
        this();
        setMin(min);
        setMax(max);
        setStep(step);
        setValue(value);
        setVertical(vertical);
        addValueChangeHandler(valueChangeHandler);
    }

    private static Element createInputElement() {
        Element input = DOM.createElement("INPUT");
        input.setAttribute("type", "range");
        return input;
    }

    public void setMin(double min) {
        getElement().setAttribute("min", Double.toString(min));
    }

    public void setMax(double max) {
        getElement().setAttribute("max", Double.toString(max));
    }

    public void setStep(double step) {
        getElement().setAttribute("step", Double.toString(step));
    }

    public void setVertical(boolean vertical) {
        if(vertical) {
            getElement().getStyle().setProperty("webkitAppearance", "slider-vertical");
        } else {
            getElement().getStyle().clearProperty("webkitAppearance");
        }
    }
}
