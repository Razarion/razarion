package com.btxtech.client.editor.generic;

import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.ui.shared.api.annotations.Templated;

@Templated("GenericPropertyPanel.html#genericPropertyPanel")
public class PropertyField {
    private String propertyName;
    private PropertyType propertyType;
    private HasProperties hasProperties;

    // Used by gwt
    public PropertyField() {
    }

    public PropertyField(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.hasProperties = hasProperties;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getValue() {
        Object value = hasProperties.get(propertyName);
        if (value == null) {
            return "-";
        }
        return value.toString();
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }
}
