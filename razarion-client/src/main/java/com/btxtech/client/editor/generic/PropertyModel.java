package com.btxtech.client.editor.generic;

import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyModel {
    private static final List<String> READ_ONLY_PROPERTIES = Collections.singletonList("id");
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
            hasProperties.set(propertyName, PropertyTypeUtils.stringToValue(value, propertyType, readOnly));
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Cannot set property value for property: " + propertyName, t);
        }
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
