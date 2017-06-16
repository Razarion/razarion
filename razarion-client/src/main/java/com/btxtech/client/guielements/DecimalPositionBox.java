package com.btxtech.client.guielements;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 01.02.2017.
 */
@Templated("DecimalPositionBox.html#decimal-position-panel")
public class DecimalPositionBox implements HasValue<DecimalPosition> {
    @Inject
    @DataField
    private CommaDoubleBox xField;
    @Inject
    @DataField
    private CommaDoubleBox yField;
    private Collection<ValueChangeHandler<DecimalPosition>> handlers = new ArrayList<>();

    @Override
    public DecimalPosition getValue() {
        if (xField.getValue() != null && yField.getValue() != null) {
            return new DecimalPosition(xField.getValue(), yField.getValue());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(DecimalPosition decimalPosition) {
        if (decimalPosition != null) {
            xField.setValue(decimalPosition.getX());
            yField.setValue(decimalPosition.getY());
        } else {
            xField.setValue(null);
            yField.setValue(null);
        }
    }

    @EventHandler("xField")
    public void xFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @EventHandler("yField")
    public void yFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @Override
    public void setValue(DecimalPosition decimalPosition, boolean fireEvents) {
        setValue(decimalPosition);
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<DecimalPosition> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<DecimalPosition> valueChangeEvent = new ValueChangeEvent<DecimalPosition>(getValue()) {
        };

        for (ValueChangeHandler<DecimalPosition> handler : handlers) {
            handler.onValueChange(valueChangeEvent);
        }
    }
}
