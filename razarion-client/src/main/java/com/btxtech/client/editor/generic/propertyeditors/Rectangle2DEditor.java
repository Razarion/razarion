package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Rectangle2D;
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

@Templated("Rectangle2DEditor.html#rectangle2d")
public class Rectangle2DEditor extends AbstractPropertyEditor<Rectangle2D> {
    // private Logger logger = Logger.getLogger(Rectangle2DEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement rectangle2d;
    @Inject
    @DataField
    private HTMLInputElement xField;
    @Inject
    @DataField
    private HTMLInputElement yField;
    @Inject
    @DataField
    private HTMLInputElement widthField;
    @Inject
    @DataField
    private HTMLInputElement heightField;

    @Override
    public void showValue() {
        if (getPropertyValue() != null) {
            xField.value = Double.toString(getPropertyValue().startX());
            yField.value = Double.toString(getPropertyValue().startY());
            widthField.value = Double.toString(getPropertyValue().width());
            heightField.value = Double.toString(getPropertyValue().height());
        }
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle2D(Double.parseDouble(xField.value), getPropertyValue().startY(), getPropertyValue().width(), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle2D(getPropertyValue().startX(), Double.parseDouble(yField.value), getPropertyValue().width(), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("widthField")
    private void onWidthFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle2D(getPropertyValue().startX(), getPropertyValue().startY(), Double.parseDouble(widthField.value), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("heightField")
    private void onHeightFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle2D(getPropertyValue().startX(), getPropertyValue().startY(), getPropertyValue().width(), Double.parseDouble(heightField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return rectangle2d;
    }
}
