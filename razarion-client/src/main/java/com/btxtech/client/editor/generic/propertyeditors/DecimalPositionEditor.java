package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.DecimalPosition;
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

@Templated("DecimalPositionEditor.html#decimalPositionPanel")
public class DecimalPositionEditor extends AbstractPropertyEditor<DecimalPosition> {
    // private Logger logger = Logger.getLogger(Rectangle2DEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement decimalPositionPanel;
    @Inject
    @DataField
    private HTMLInputElement xField;
    @Inject
    @DataField
    private HTMLInputElement yField;

    @Override
    public void showValue() {
        xField.value = Double.toString(getPropertyValue().getX());
        yField.value = Double.toString(getPropertyValue().getY());
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new DecimalPosition(Double.parseDouble(xField.value), Double.parseDouble(yField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new DecimalPosition(Double.parseDouble(xField.value), Double.parseDouble(yField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return decimalPositionPanel;
    }
}
