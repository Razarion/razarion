package com.btxtech.client.guielements;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasValue;
import elemental.html.SpanElement;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 01.02.2017.
 */
@Templated("VertexRoBox.html#vertex-panel")
public class VertexRoBox implements HasValue<Vertex> {
    @Inject
    @DataField
    private Span xField;
    @Inject
    @DataField
    private Span yField;
    @Inject
    @DataField
    private Span zField;
    private Vertex vertex;

    @Override
    public Vertex getValue() {
        return vertex;
    }

    @Override
    public void setValue(Vertex vertex) {
        this.vertex = vertex;
        if (vertex != null) {
            xField.setTextContent(DisplayUtils.handleDouble2(vertex.getX()));
            yField.setTextContent(DisplayUtils.handleDouble2(vertex.getY()));
            zField.setTextContent(DisplayUtils.handleDouble2(vertex.getZ()));
        } else {
            xField.setTextContent(null);
            yField.setTextContent(null);
            zField.setTextContent(null);
        }
    }

    @Override
    public void setValue(Vertex vertex, boolean fireEvents) {
        setValue(vertex);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Vertex> handler) {
        return () -> {
        };
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
    }
}
