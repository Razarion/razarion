package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.EnumEditor;
import com.btxtech.client.editor.generic.propertyeditors.ListEditor;
import com.btxtech.client.editor.generic.propertyeditors.PropertyEditorClassFactory;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.client.editor.generic.propertyeditors.UnknownEditor;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

@Dependent
public class PropertyModel {
    @Inject
    private Instance<PropertyModel> metaModelContextInstance;
    private HasProperties hasProperties;
    private String propertyName;
    private PropertyType propertyType;
    private Integer listIndex;


    public void initAsRoot(Object rootPropertyValue) {
        hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(rootPropertyValue);
        propertyType = new PropertyType(rootPropertyValue.getClass(), true, false);
    }

    private void initAsSectionChild(PropertyModel parent, String propertyName, PropertyType propertyType) {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        if (propertyType.isBindable()) {
            hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(propertyType.getType());
        } else {
            hasProperties = parent.hasProperties;
        }
    }

    private void initAsListChild(int listIndex, PropertyType propertyType) {
        this.listIndex = listIndex;
        this.propertyType = propertyType;
//        xxx
//
//        if (propertyType.isBindable()) {
//            hasProperties = (HasProperties) BindableProxyFactory.getBindableProxy(propertyType.getType());
//        } else {
//            hasProperties = parent.hasProperties;
//        }
    }

    public void createBindableChildren(Consumer<PropertyModel> childConsumer) {
        if (!propertyType.isBindable()) {
            throw new IllegalStateException("Property is not bindable: " + this);
        }
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            PropertyModel childPropertyModel = metaModelContextInstance.get();
            childPropertyModel.initAsSectionChild(this, propertyName, propertyType);
            childConsumer.accept(childPropertyModel);
        });
    }

    public void createListChildren(Consumer<PropertyModel> childConsumer) {
        if (!propertyType.isList()) {
            throw new IllegalStateException("Property is not a list: " + this);
        }
        for (int index = 0; index < ((List) getPropertyValue()).size(); index++) {
            PropertyModel childPropertyModel = metaModelContextInstance.get();
            childPropertyModel.initAsListChild(index, propertyType);
            childConsumer.accept(childPropertyModel);
        }
    }

    public String getDisplayName() {
        if (propertyName == null) {
            throw new IllegalStateException("Root property has no display name: " + this);
        }
        return propertyName;
    }

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

    public Object getPropertyValue() {
        if (propertyName != null) {
            return hasProperties.get(propertyName);
        } else {
            return hasProperties;
        }
    }

    public void setPropertyValue(Object value) {
        if (propertyName == null) {
            throw new IllegalStateException("Root property can not be set: " + this);
        }
        hasProperties.set(propertyName, value);
    }

    public void createAndSetPropertyValue() {
        setPropertyValue(BindableProxyFactory.getBindableProxy(propertyType.getType()));
    }

    public boolean isPropertyNullable() {
        return propertyType.isBindable();
    }

    public boolean isPropertyValueNotNull() {
        return getPropertyValue() != null;
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
        return "PropertyModel{" +
                ", propertyName='" + propertyName + '\'' +
                ", propertyType.getType()=" + propertyType.getType() +
                ", propertyType.isBindable()=" + propertyType.isBindable() +
                '}';
    }
}
