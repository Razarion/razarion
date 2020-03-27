package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.editor.generic.model.Branch;
import com.btxtech.shared.system.ExceptionHandler;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.inject.Inject;

public abstract class AbstractPropertyEditor<T> implements IsElement {
    @Inject
    private ExceptionHandler exceptionHandler;
    private AbstractPropertyModel abstractPropertyModel;

    public void init(AbstractPropertyModel abstractPropertyModel) {
        this.abstractPropertyModel = abstractPropertyModel;
        try {
            showValue();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    protected abstract void showValue();

    protected void setPropertyValue(T object) {
        abstractPropertyModel.setPropertyValue(object);
    }

    protected T getPropertyValue() {
        return (T) abstractPropertyModel.getPropertyValue();
    }

    protected T getPropertyValue(T defaultValue) {
        T t = (T) abstractPropertyModel.getPropertyValue();
        if (t != null) {
            return t;
        } else {
            return defaultValue;
        }
    }

    protected String getPropertyValueString() {
        return abstractPropertyModel.getPropertyValueString();
    }

    protected AbstractPropertyModel getAbstractPropertyModel() {
        return abstractPropertyModel;
    }

    protected Branch getBranch() {
        return (Branch) getAbstractPropertyModel();
    }

}
