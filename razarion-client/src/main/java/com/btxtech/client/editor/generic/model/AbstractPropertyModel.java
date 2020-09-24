package com.btxtech.client.editor.generic.model;

import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.EnumEditor;
import com.btxtech.client.editor.generic.propertyeditors.ListEditor;
import com.btxtech.client.editor.generic.propertyeditors.IntegerMapEditor;
import com.btxtech.client.editor.generic.propertyeditors.PropertyEditorClassFactory;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.client.editor.generic.propertyeditors.UnknownEditor;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.PropertyType;

import java.util.Map;

public abstract class AbstractPropertyModel {
    // private static Logger logger = Logger.getLogger(AbstractPropertyModel.class.getName());
    private PropertyType propertyType;

    protected void initInternal(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public abstract String getDisplayName();

    public abstract Object getPropertyValue();

    public abstract boolean isPropertyNullable();

    public abstract void setPropertyValue(Object value);

    public Class<? extends AbstractPropertyEditor> getEditorClass() {
        if (propertyType.getType().isEnum()) {
            return EnumEditor.class;
        } else if (propertyType.isBindable()) {
            return PropertySection.class;
        } else if (propertyType.isList()) {
            return ListEditor.class;
        } else if (propertyType.getType().equals(Map.class)) {
            return IntegerMapEditor.class;
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

    public boolean isPropertyValueNotNull() {
        return getPropertyValue() != null;
    }

    public void createAndSetPropertyValue() {
        setPropertyValue(BindableProxyFactory.getBindableProxy(propertyType.getType()));
    }

    public String getPropertyValueString() {
        if (isPropertyValueNotNull()) {
            return getPropertyValue().toString();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "AbstractPropertyModel{propertyType.getType()=" + propertyType.getType() +
                ", propertyType.isBindable()=" + propertyType.isBindable() +
                '}';
    }
}
