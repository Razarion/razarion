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

@Templated("GenericPropertyBook.html#genericPropertyBook")
public class PropertyWidget implements IsElement, TakesValue<PropertyModel> {
    // private Logger logger = Logger.getLogger(PropertyWidget.class.getName());
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
    private PropertyModel propertyModel;

    @Override
    public HTMLElement getElement() {
        return Js.cast(propertyTableRow);
    }

    @Override
    public void setValue(PropertyModel propertyModel) {
        this.propertyModel = propertyModel;
        propertyName.textContent = propertyModel.getPropertyName();
        Elemental2Utils.removeAllChildren(propertyValue);
        Class propertyClass = propertyModel.getPropertyType().getType();
        if(propertyModel.isReadOnly()) {
            propertyValue.textContent = propertyModel.getStringValue();
        } else if (propertyClass == String.class) {
            propertyValue.appendChild(setupStringEditor(propertyModel));
        } else if (propertyClass == Integer.class) {
            propertyValue.appendChild(setupIntegerEditor(propertyModel));
        } else if (propertyClass == Double.class) {
            propertyValue.appendChild(setupDoubleEditor(propertyModel));
        } else {
            propertyValue.textContent = setupUnknownInformation(propertyClass);
        }
    }

    private Node setupStringEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event ->  {
            propertyModel.setStringValue(htmlInputElement.value);
        }, false);
        return htmlInputElement;
    }

    private HTMLInputElement setupIntegerEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event ->  {
            propertyModel.setStringValue(htmlInputElement.value);
        }, false);
        return htmlInputElement;
    }

    private Node setupDoubleEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event ->  {
            propertyModel.setStringValue(htmlInputElement.value);
        }, false);
        return htmlInputElement;
    }

    private String setupUnknownInformation(Class propertyClass) {
        return "No editor for <" + propertyClass + ">";
    }

    @Override
    public PropertyModel getValue() {
        return propertyModel;
    }
}
