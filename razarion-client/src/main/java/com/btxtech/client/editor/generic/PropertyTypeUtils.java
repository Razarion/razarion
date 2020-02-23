package com.btxtech.client.editor.generic;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Node;
import org.jboss.errai.databinding.client.PropertyType;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public final class PropertyTypeUtils {
    private PropertyTypeUtils() {
    }

    public static Object stringToValue(String value, PropertyType propertyType, boolean readOnly) {
        if (readOnly) {
            throw new IllegalStateException("Readonly property can not be set");
        }
        if (propertyType.getType() == String.class) {
            return value;
        } else if (propertyType.getType() == Integer.class) {
            return parseInt(value);
        } else if (propertyType.getType() == Double.class) {
            return parseDouble(value);
        } else {
            throw new IllegalStateException("Can not handle propertyType.getType(): " + propertyType.getType());
        }
    }

    public static Node setupPropertyWidget(PropertyModel propertyModel) {
        Class propertyClass = propertyModel.getPropertyType().getType();

        if (propertyModel.isReadOnly()) {
            return setupReadonlyDiv(propertyModel.getStringValue());
        } else if (propertyClass == String.class) {
            return setupStringEditor(propertyModel);
        } else if (propertyClass == Integer.class) {
            return setupIntegerEditor(propertyModel);
        } else if (propertyClass == Double.class) {
            return setupDoubleEditor(propertyModel);
        } else {
            return setupUnknownInformation(propertyClass);
        }
    }

    private static Node setupReadonlyDiv(String stringValue) {
        HTMLDivElement divElement = (HTMLDivElement) DomGlobal.document.createElement("div");
        divElement.textContent = stringValue;
        return divElement;
    }

    public static Node setupUnknownInformation(Class propertyClass) {
        HTMLDivElement divElement = (HTMLDivElement) DomGlobal.document.createElement("div");
        divElement.textContent = "No editor for <" + propertyClass + ">";
        return divElement;
    }

    public static Node setupStringEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event -> propertyModel.setStringValue(htmlInputElement.value), false);
        return htmlInputElement;
    }

    public static HTMLInputElement setupIntegerEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event -> propertyModel.setStringValue(htmlInputElement.value), false);
        return htmlInputElement;
    }

    public static Node setupDoubleEditor(PropertyModel propertyModel) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = propertyModel.getStringValue();
        htmlInputElement.addEventListener("input", event -> propertyModel.setStringValue(htmlInputElement.value), false);
        return htmlInputElement;
    }

}

