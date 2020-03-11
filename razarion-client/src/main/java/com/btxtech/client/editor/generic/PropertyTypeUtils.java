package com.btxtech.client.editor.generic;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLInputElement;
import elemental2.dom.Node;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public final class PropertyTypeUtils {
    private static Logger LOGGER = Logger.getLogger(PropertyTypeUtils.class.getName());
    private static final List<String> READ_ONLY_PROPERTIES = Collections.singletonList("id");

    private PropertyTypeUtils() {
    }

    public static boolean isPrimitiveProperty(PropertyType propertyType) {
        if (propertyType.getType() == String.class) {
            return true;
        } else if (propertyType.getType() == Integer.class) {
            return true;
        } else {
            return propertyType.getType() == Double.class;
        }
    }

    public static boolean isBindableProperty(PropertyType propertyType) {
        try {
            BindableProxyFactory.getBindableProxy(propertyType);
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    public static boolean isReadOnly(String propertyName) {
        return READ_ONLY_PROPERTIES.stream().anyMatch(s -> s.equalsIgnoreCase(propertyName));
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

    public static String readStringValue(String propertyName, HasProperties hasProperties) {
        Object value = hasProperties.get(propertyName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static void writeStringValue(String propertyName, String value, PropertyType propertyType, HasProperties hasProperties) {
        try {
            hasProperties.set(propertyName, stringToValue(value, propertyType, isReadOnly(propertyName)));
        } catch (Throwable t) {
            LOGGER.log(Level.WARNING, "Cannot set property value for property: " + propertyName, t);
        }
    }


    public static Node setupPropertyWidget(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        Class propertyClass = propertyType.getType();

        if (PropertyTypeUtils.isReadOnly(propertyName)) {
            return setupReadonlyDiv(propertyName, hasProperties);
        } else if (propertyClass == String.class) {
            return setupStringEditor(propertyName, propertyType, hasProperties);
        } else if (propertyClass == Integer.class) {
            return setupIntegerEditor(propertyName, propertyType, hasProperties);
        } else if (propertyClass == Double.class) {
            return setupDoubleEditor(propertyName, propertyType, hasProperties);
        } else {
            return setupUnknownInformation(propertyClass);
        }
    }

    private static Node setupReadonlyDiv(String propertyName, HasProperties hasProperties) {
        HTMLDivElement divElement = (HTMLDivElement) DomGlobal.document.createElement("div");
        divElement.textContent = readStringValue(propertyName, hasProperties);
        return divElement;
    }

    public static Node setupUnknownInformation(Class propertyClass) {
        HTMLDivElement divElement = (HTMLDivElement) DomGlobal.document.createElement("div");
        divElement.textContent = "No editor for <" + propertyClass + ">";
        return divElement;
    }

    public static Node setupStringEditor(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.value = readStringValue(propertyName, hasProperties);
        htmlInputElement.addEventListener("input", event -> writeStringValue(propertyName, htmlInputElement.value, propertyType, hasProperties), false);
        return htmlInputElement;
    }

    public static HTMLInputElement setupIntegerEditor(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = readStringValue(propertyName, hasProperties);
        htmlInputElement.addEventListener("input", event -> writeStringValue(propertyName, htmlInputElement.value, propertyType, hasProperties), false);
        return htmlInputElement;
    }

    public static Node setupDoubleEditor(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        HTMLInputElement htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.value = readStringValue(propertyName, hasProperties);
        htmlInputElement.addEventListener("input", event -> writeStringValue(propertyName, htmlInputElement.value, propertyType, hasProperties), false);
        return htmlInputElement;
    }
}

