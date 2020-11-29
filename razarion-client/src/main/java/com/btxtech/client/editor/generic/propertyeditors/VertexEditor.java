package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.Event;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("VertexEditor.html#vertexEditorPanel")
public class VertexEditor extends AbstractPropertyEditor<Vertex> {
    // private Logger logger = Logger.getLogger(VertexEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement vertexEditorPanel;
    @Inject
    @DataField
    private HTMLInputElement xField;
    @Inject
    @DataField
    private HTMLInputElement yField;
    @Inject
    @DataField
    private HTMLInputElement zField;

    @Override
    public void showValue() {
        if (getPropertyValue() != null) {
            xField.value = Double.toString(getPropertyValue().getX());
            yField.value = Double.toString(getPropertyValue().getY());
            zField.value = Double.toString(getPropertyValue().getY());
        }
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Vertex(Double.parseDouble(xField.value), Double.parseDouble(yField.value), Double.parseDouble(zField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Vertex(Double.parseDouble(xField.value), Double.parseDouble(yField.value), Double.parseDouble(zField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("zField")
    private void onZFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Vertex(Double.parseDouble(xField.value), Double.parseDouble(yField.value), Double.parseDouble(zField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return vertexEditorPanel;
    }
}
