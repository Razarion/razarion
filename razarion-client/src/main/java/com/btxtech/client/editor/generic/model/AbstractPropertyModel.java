package com.btxtech.client.editor.generic.model;

import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
import com.btxtech.client.editor.generic.propertyeditors.EnumEditor;
import com.btxtech.client.editor.generic.propertyeditors.ImageIdEditor;
import com.btxtech.client.editor.generic.propertyeditors.IntegerMapEditor;
import com.btxtech.client.editor.generic.propertyeditors.ListEditor;
import com.btxtech.client.editor.generic.propertyeditors.PropertyEditorClassFactory;
import com.btxtech.client.editor.generic.propertyeditors.PropertySection;
import com.btxtech.client.editor.generic.propertyeditors.UnknownEditor;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.editor.OpenApi3Schema;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.PropertyType;

import javax.inject.Inject;
import java.util.Map;

public abstract class AbstractPropertyModel {
    // private static Logger logger = Logger.getLogger(AbstractPropertyModel.class.getName());
    @Inject
    private GenericPropertyInfoProvider genericPropertyInfoProvider;
    private PropertyType propertyType;

    protected void initInternal(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    protected abstract String getPropertyName();

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

            if (this instanceof Leaf && propertyType.getType() == Integer.class && getPropertyName() != null) {
                Class parentClass = (((Leaf)this).getBranch()).getPropertyClass();
                OpenApi3Schema openApi3Schema = genericPropertyInfoProvider.scanForOpenApiScheme(parentClass, getPropertyName());
                if (openApi3Schema != null && CommonUrl.IMAGE_ID_TYPE.equals(openApi3Schema.getType())) {
                    return ImageIdEditor.class;
                }
            }
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
