package com.btxtech.client.dialogs;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HorizontalPanel;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 11.04.2015.
 */
public class HorizontalInputRangeNumber extends HorizontalPanel {
    private InputNumber inputNumber;
    private InputRange inputRange;
    private Collection<ValueChangeHandler<Double>> valueChangeHandlers = new ArrayList<>();

    public HorizontalInputRangeNumber() {
        setHorizontalAlignment(ALIGN_CENTER);
        setVerticalAlignment(ALIGN_MIDDLE);

        inputNumber = new InputNumber();

        inputRange = new InputRange();
        inputRange.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                inputNumber.setValue(event.getValue());
                fireChange(event);
            }
        });
        inputRange.setVertical(false);
        add(inputRange);

        inputNumber.addValueChangeHandler(new ValueChangeHandler<Double>() {
            @Override
            public void onValueChange(ValueChangeEvent<Double> event) {
                inputRange.setValue(event.getValue());
                fireChange(event);
            }
        });
        add(inputNumber);
    }

    public void setStep(double value) {
        inputNumber.setStep(value);
        inputRange.setStep(value);
    }

    public void setMin(double value) {
        inputNumber.setMin(value);
        inputRange.setMin(value);
    }

    public void setMax(double value) {
        inputNumber.setMax(value);
        inputRange.setMax(value);
    }

    public void setValue(double value) {
        inputNumber.setValue(value);
        inputRange.setValue(value);
    }

    public HandlerRegistration addValueChangeHandler(final ValueChangeHandler<Double> handler) {
        valueChangeHandlers.add(handler);
        return new HandlerRegistration() {
            @Override
            public void removeHandler() {
                valueChangeHandlers.remove(handler);
            }
        };
    }

    private void fireChange(ValueChangeEvent<Double> event) {
        for (ValueChangeHandler<Double> valueChangeHandler : valueChangeHandlers) {
            valueChangeHandler.onValueChange(event);
        }
    }

}
