package com.btxtech.client.guielements;

import com.btxtech.shared.datatypes.Vertex;
import com.google.gwt.user.client.ui.DoubleBox;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 01.02.2017.
 */
@Templated("VertexBox.html#vertex-panel")
public class VertexBox {
    @Inject
    @DataField
    private DoubleBox xField;
    @Inject
    @DataField
    private DoubleBox yField;
    @Inject
    @DataField
    private DoubleBox zField;

    public Vertex getVertex() {
        if (xField.getValue() != null && yField.getValue() != null && zField.getValue() != null) {
            return new Vertex(xField.getValue(), yField.getValue(), zField.getValue());
        } else {
            return null;
        }
    }

}
