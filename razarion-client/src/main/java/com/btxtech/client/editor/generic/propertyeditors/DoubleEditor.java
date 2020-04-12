package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class DoubleEditor extends AbstractPropertyEditor<Double> {
    @Inject
    private ExceptionHandler exceptionHandler;
    private HTMLInputElement htmlInputElement;

    @PostConstruct
    public void postConstruct() {
        htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
        htmlInputElement.step = "0.1";
        htmlInputElement.addEventListener("input", event -> {
            try {
                if (htmlInputElement.value != null && !htmlInputElement.value.trim().isEmpty()) {
                    setPropertyValue(Double.parseDouble(htmlInputElement.value));
                } else {
                    setPropertyValue(null);
                }
            } catch (Throwable t) {
                exceptionHandler.handleException("Cannot set property value for property: " + getAbstractPropertyModel(), t);
            }
        }, false);
    }

    @Override
    public void showValue() {
        htmlInputElement.value = getPropertyValueString();
    }


    @Override
    public HTMLElement getElement() {
        return htmlInputElement;
    }
}
