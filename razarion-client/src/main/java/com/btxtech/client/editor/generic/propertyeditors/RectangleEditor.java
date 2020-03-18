package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.Event;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("RectangleEditor.html#rectangle")
public class RectangleEditor implements GenericPropertyEditor {
    // private Logger logger = Logger.getLogger(rectangleEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLDivElement rectangle;
    @Inject
    @DataField
    private HTMLTableElement table;
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
    @Inject
    @DataField
    private HTMLDivElement nullDiv;
    @Inject
    @DataField
    private HTMLButtonElement createButton;
    private String propertyName;
    private HasProperties hasProperties;

    @Override
    public void init(String propertyName, Class propertyClass, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.hasProperties = hasProperties;
        Rectangle rectangle = (Rectangle) hasProperties.get(propertyName);
        setValueInternal(rectangle);
    }

    private void setValueInternal(Rectangle rectangle) {
        if (rectangle != null) {
            nullDiv.style.display = "none";
            table.style.display = "table";
            xField.value = Integer.toString(rectangle.startX());
            yField.value = Integer.toString(rectangle.startY());
            widthField.value = Integer.toString(rectangle.width());
            heightField.value = Integer.toString(rectangle.height());
        } else {
            nullDiv.style.display = "block";
            table.style.display = "none";
        }
    }

    @EventHandler("createButton")
    private void onCreateButtonClicked(ClickEvent event) {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        setValueInternal(rectangle);
        hasProperties.set(propertyName, rectangle);
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle rectangle= ((Rectangle) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle(Integer.parseInt(xField.value), rectangle.startY(), rectangle.width(), rectangle.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle rectangle= ((Rectangle) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle(rectangle.startX(), Integer.parseInt(yField.value), rectangle.width(), rectangle.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("widthField")
    private void onWidthFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle rectangle= ((Rectangle) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle(rectangle.startX(), rectangle.startY(), Integer.parseInt(widthField.value), rectangle.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("heightField")
    private void onHeightFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle rectangle= ((Rectangle) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle(rectangle.startX(), rectangle.startY(), rectangle.width(), Integer.parseInt(heightField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return rectangle;
    }
}
