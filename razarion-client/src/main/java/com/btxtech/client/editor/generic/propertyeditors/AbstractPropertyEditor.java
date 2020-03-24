package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.PropertyModel;
import com.btxtech.shared.system.ExceptionHandler;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.inject.Inject;

public abstract class AbstractPropertyEditor<T> implements IsElement {
    @Inject
    private ExceptionHandler exceptionHandler;
    private PropertyModel propertyModel;

    public void init(PropertyModel propertyModel) {
        this.propertyModel = propertyModel;
        try {
            showValue();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    protected abstract void showValue();

    protected void setPropertyValue(T object) {
        propertyModel.setPropertyValue(object);
    }

    protected T getPropertyValue() {
        return (T) propertyModel.getPropertyValue();
    }

    protected T getPropertyValue(T defaultValue) {
        T t = (T) propertyModel.getPropertyValue();
        if(t != null) {
            return t;
        } else {
            return defaultValue;
        }
    }

    protected String getPropertyValueString() {
        return propertyModel.getPropertyValueString();
    }

    protected PropertyModel getPropertyModel() {
        return propertyModel;
    }
}
