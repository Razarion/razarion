package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Index;
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

@Templated("IndexEditor.html#indexPanel")
public class IndexEditor extends AbstractPropertyEditor<Index> {
    // private Logger logger = Logger.getLogger(Rectangle2DEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement indexPanel;
    @Inject
    @DataField
    private HTMLInputElement xField;
    @Inject
    @DataField
    private HTMLInputElement yField;

    @Override
    public void showValue() {
        if (getPropertyValue() != null) {
            xField.value = Integer.toString(getPropertyValue().getY());
            yField.value = Integer.toString(getPropertyValue().getY());
        }
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Index(Integer.parseInt(xField.value), Integer.parseInt(yField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Index(Integer.parseInt(xField.value), Integer.parseInt(yField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return indexPanel;
    }
}
