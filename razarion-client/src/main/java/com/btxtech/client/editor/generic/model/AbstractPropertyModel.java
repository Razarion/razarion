package com.btxtech.client.editor.generic.model;

import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.EnumEditor;
import com.btxtech.client.editor.generic.propertyeditors.ListEditor;
import com.btxtech.client.editor.generic.propertyeditors.PropertyEditorClassFactory;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.client.editor.generic.propertyeditors.UnknownEditor;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

public abstract class AbstractPropertyModel {
    private HasProperties hasProperties;
    private String propertyName;
    private PropertyType propertyType;
    private Integer listIndex;

    protected void initInternal(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public abstract String getDisplayName();

    public abstract Object getPropertyValue();

    public Class<? extends AbstractPropertyEditor> getEditorClass() {
        if (propertyType.getType().isEnum()) {
            return EnumEditor.class;
        } else if (propertyType.isBindable()) {
            return PropertySection.class;
        } else if (propertyType.isList()) {
            return ListEditor.class;
        } else {
            Class<? extends AbstractPropertyEditor> propertyEditorClass = PropertyEditorClassFactory.get(propertyType.getType());
            if (propertyEditorClass == null) {
                return UnknownEditor.class;
            }
            return propertyEditorClass;
        }
    }

    public Class getPropertyClass() {
        return propertyType.getType();
    }

    protected PropertyType getPropertyType() {
        return propertyType;
    }

    //------------------------------

    public void setPropertyValue(Object value) { // TODO
        if (propertyName == null) {
            throw new IllegalStateException("Root property can not be set: " + this);
        }
        hasProperties.set(propertyName, value);
    }

    public void createAndSetPropertyValue() { // TODO
        setPropertyValue(BindableProxyFactory.getBindableProxy(propertyType.getType()));
    }

    public boolean isPropertyNullable() { // TODO
        return propertyType.isBindable();
    }

    public boolean isPropertyValueNotNull() { // TODO
        return getPropertyValue() != null;
    }

    public String getPropertyValueString() { // TODO
        if (isPropertyValueNotNull()) {
            return getPropertyValue().toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AbstractPropertyModel{propertyName=" + propertyName +
                ", listIndex=" + listIndex +
                ", propertyType.getType()=" + propertyType.getType() +
                ", propertyType.isBindable()=" + propertyType.isBindable() +
                '}';
    }
}
