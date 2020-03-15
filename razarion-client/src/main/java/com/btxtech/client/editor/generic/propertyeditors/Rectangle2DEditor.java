package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Rectangle2D;
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

@Templated("Rectangle2DEditor.html#rectangle2d")
public class Rectangle2DEditor implements GenericPropertyEditor {
    // private Logger logger = Logger.getLogger(Rectangle2DEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    @DataField
    private HTMLDivElement rectangle2d;
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
    public void init(String propertyName, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.hasProperties = hasProperties;
        Rectangle2D rectangle2D = (Rectangle2D) hasProperties.get(propertyName);
        setValueInternal(rectangle2D);
    }

    private void setValueInternal(Rectangle2D rectangle2D) {
        if (rectangle2D != null) {
            nullDiv.style.display = "none";
            table.style.display = "table";
            xField.value = Double.toString(rectangle2D.startX());
            yField.value = Double.toString(rectangle2D.startY());
            widthField.value = Double.toString(rectangle2D.width());
            heightField.value = Double.toString(rectangle2D.height());
        } else {
            nullDiv.style.display = "block";
            table.style.display = "none";
        }
    }

    @EventHandler("createButton")
    private void onCreateButtonClicked(ClickEvent event) {
        Rectangle2D rectangle2D = new Rectangle2D(0, 0, 1, 1);
        setValueInternal(rectangle2D);
        hasProperties.set(propertyName, rectangle2D);
    }

    @EventHandler("xField")
    private void onXFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle2D rectangle2D = ((Rectangle2D) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle2D(Double.parseDouble(xField.value), rectangle2D.startY(), rectangle2D.width(), rectangle2D.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("yField")
    private void onYFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle2D rectangle2D = ((Rectangle2D) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle2D(rectangle2D.startX(), Double.parseDouble(yField.value), rectangle2D.width(), rectangle2D.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("widthField")
    private void onWidthFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle2D rectangle2D = ((Rectangle2D) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle2D(rectangle2D.startX(), rectangle2D.startY(), Double.parseDouble(widthField.value), rectangle2D.height()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @EventHandler("heightField")
    private void onHeightFieldChanged(@ForEvent("input") Event e) {
        try {
            Rectangle2D rectangle2D = ((Rectangle2D) hasProperties.get(propertyName));
            hasProperties.set(propertyName, new Rectangle2D(rectangle2D.startX(), rectangle2D.startY(), rectangle2D.width(), Double.parseDouble(heightField.value)));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return rectangle2d;
    }
}
