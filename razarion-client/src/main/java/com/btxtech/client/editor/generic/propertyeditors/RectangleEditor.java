package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Rectangle;
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

@Templated("RectangleEditor.html#rectangle")
public class RectangleEditor extends AbstractPropertyEditor<Rectangle> {
    // private Logger logger = Logger.getLogger(rectangleEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLTableElement rectangle;
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
        xField.value = Integer.toString(getPropertyValue().startX());
        yField.value = Integer.toString(getPropertyValue().startY());
        widthField.value = Integer.toString(getPropertyValue().width());
        heightField.value = Integer.toString(getPropertyValue().height());
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle(Integer.parseInt(xField.value), getPropertyValue().startY(), getPropertyValue().width(), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle(getPropertyValue().startX(), Integer.parseInt(yField.value), getPropertyValue().width(), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("widthField")
    private void onWidthFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle(getPropertyValue().startX(), getPropertyValue().startY(), Integer.parseInt(widthField.value), getPropertyValue().height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("heightField")
    private void onHeightFieldChanged(@ForEvent("input") Event e) {
        try {
            setPropertyValue(new Rectangle(getPropertyValue().startX(), getPropertyValue().startY(), getPropertyValue().width(), Integer.parseInt(heightField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return rectangle;
    }
}
