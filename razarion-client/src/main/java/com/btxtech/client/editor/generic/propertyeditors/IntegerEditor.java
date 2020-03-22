package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class IntegerEditor extends AbstractPropertyEditor<Integer> {
    @Inject
    private ExceptionHandler exceptionHandler;
    private HTMLInputElement htmlInputElement;

    @PostConstruct
    public void postConstruct() {
        htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "number";
    }

    @Override
    public void showValue() {
        htmlInputElement.value = getPropertyValueString();

        htmlInputElement.addEventListener("input", event -> {
            try {
                setPropertyValue(Integer.parseInt(htmlInputElement.value));
            } catch (Throwable t) {
                exceptionHandler.handleException("Cannot set property value for property: " + getPropertyModel(), t);
            }
        }, false);
    }

    @Override
    public HTMLElement getElement() {
        return htmlInputElement;
    }
}
