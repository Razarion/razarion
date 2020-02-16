package com.btxtech.client.editor.generic;

import com.btxtech.client.utils.Elemental2Utils;
import com.google.gwt.user.client.TakesValue;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.Node;
import jsinterop.base.Js;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

@Templated("GenericPropertyPanel.html#genericPropertyPanel")
public class PropertyFieldWidget implements IsElement, TakesValue<PropertyField> {
    @Inject
    @DataField
    private HTMLTableRowElement propertyTableRow;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyName;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyValue;
    private PropertyField propertyField;

    @Override
    public HTMLElement getElement() {
        return Js.cast(propertyTableRow);
    }

    @Override
    public void setValue(PropertyField propertyField) {
        this.propertyField = propertyField;
        propertyName.textContent = propertyField.getPropertyName();
        Elemental2Utils.removeAllChildren(propertyValue);
        Class propertyClass = propertyField.getPropertyType().getType();
        if (propertyClass == String.class) {
            propertyValue.appendChild(setupStringEditor(propertyField));
        } else if (propertyClass == Integer.class) {
            propertyValue.appendChild(setupIntegerEditor(propertyField));
        } else if (propertyClass == Double.class) {
            propertyValue.appendChild(setupDoubleEditor(propertyField));
        } else {
            propertyValue.textContent = setupUnknownInformation(propertyClass);
        }
    }

    private Node setupStringEditor(PropertyField propertyField) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.value = propertyField.getValue();
        return htmlInputElement;
    }

    private HTMLInputElement setupIntegerEditor(PropertyField propertyField) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyField.getValue();
        return htmlInputElement;
    }

    private Node setupDoubleEditor(PropertyField propertyField) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyField.getValue();
        return htmlInputElement;
    }

    private String setupUnknownInformation(Class propertyClass) {
        return "No editor for <" + propertyField + ">";
    }

    @Override
    public PropertyField getValue() {
        return propertyField;
    }
}
