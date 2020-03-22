package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.PropertyModel;
import org.jboss.errai.common.client.api.elemental2.IsElement;

public abstract class AbstractPropertyEditor<T> implements IsElement {
    private PropertyModel propertyModel;

    public void init(PropertyModel propertyModel) {
        this.propertyModel = propertyModel;
        showValue();
    }

    protected abstract void showValue();

    protected void setPropertyValue(T object) {
        propertyModel.setPropertyValue(object);
    }

    protected T getPropertyValue() {
        return (T)propertyModel.getPropertyValue();
    }

    protected String getPropertyValueString() {
        return propertyModel.getPropertyValueString();
    }

    protected PropertyModel getPropertyModel() {
        return propertyModel;
    }
}
