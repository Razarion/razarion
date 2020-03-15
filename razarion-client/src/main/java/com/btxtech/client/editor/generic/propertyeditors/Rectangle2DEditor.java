package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("Rectangle2DEditor.html#recatngle2d")
public class Rectangle2DEditor implements GenericPropertyEditor {
    @Inject
    @DataField
    private HTMLDivElement recatngle2d;
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

    @Override
    public HTMLElement getElement() {
        return recatngle2d;
    }
}
