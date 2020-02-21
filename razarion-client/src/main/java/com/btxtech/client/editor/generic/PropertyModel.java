package com.btxtech.client.editor.generic;

import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class PropertyModel {
    private final List<String> READ_ONLY_PROPERTIES = Collections.singletonList("id");
    private Logger logger = Logger.getLogger(PropertyModel.class.getName());
    private String propertyName;
    private PropertyType propertyType;
    private HasProperties hasProperties;
    private boolean readOnly;

    // Used by gwt
    public PropertyModel() {
    }

    public PropertyModel(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.hasProperties = hasProperties;
        readOnly = READ_ONLY_PROPERTIES.stream().anyMatch(s -> s.equalsIgnoreCase(propertyName));
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getStringValue() {
        Object value = hasProperties.get(propertyName);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public void setStringValue(String value) {
        try {
            if (readOnly) {
                throw new IllegalStateException("Readonly property can not be set: " + propertyName);
            }
            if (propertyType.getType() == String.class) {
                hasProperties.set(propertyName, value);
            } else if (propertyType.getType() == Integer.class) {
                hasProperties.set(propertyName, parseInt(value));
            } else if (propertyType.getType() == Double.class) {
                hasProperties.set(propertyName, parseDouble(value));
            } else {
                logger.severe("PropertyModel.setStringValue() Can not handle propertyType.getType(): " + propertyType.getType());
            }
        } catch (Throwable t) {
            logger.log(Level.WARNING, t.getMessage(), t);
        }
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
