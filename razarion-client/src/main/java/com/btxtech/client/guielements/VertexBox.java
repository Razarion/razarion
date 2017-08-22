package com.btxtech.client.guielements;

import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.DoubleBox;
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
@Templated("VertexBox.html#vertex-panel")
public class VertexBox implements HasValue<Vertex> {
    @Inject
    @DataField
    private CommaDoubleBox xField;
    @Inject
    @DataField
    private CommaDoubleBox yField;
    @Inject
    @DataField
    private CommaDoubleBox zField;
    private Collection<ValueChangeHandler<Vertex>> handlers = new ArrayList<>();

    @Override
    public Vertex getValue() {
        if (xField.getValue() != null && yField.getValue() != null && zField.getValue() != null) {
            return new Vertex(xField.getValue(), yField.getValue(), zField.getValue());
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Vertex vertex) {
        if (vertex != null) {
            xField.setValue(vertex.getX());
            yField.setValue(vertex.getY());
            zField.setValue(vertex.getZ());
        } else {
            xField.setValue(null);
            yField.setValue(null);
            zField.setValue(null);
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

    @EventHandler("zField")
    public void zFieldChanged(ChangeEvent e) {
        fireEvent(null);
    }

    @Override
    public void setValue(Vertex vertex, boolean fireEvents) {
        setValue(vertex);
        if (fireEvents) {
            fireEvent(null);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Vertex> handler) {
        handlers.add(handler);
        return () -> handlers.remove(handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        ValueChangeEvent<Vertex> valueChangeEvent = new ValueChangeEvent<Vertex>(getValue()) {
        };

        for (ValueChangeHandler<Vertex> handler : handlers) {
            handler.onValueChange(valueChangeEvent);
        }
    }
}
