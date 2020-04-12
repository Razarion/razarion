package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class BooleanEditor extends AbstractPropertyEditor<Boolean> {
    // private Logger logger = Logger.getLogger(BooleanEditor.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    private HTMLInputElement htmlInputElement;

    @PostConstruct
    public void postConstruct() {
        htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
        htmlInputElement.type = "checkbox";
        htmlInputElement.addEventListener("input", event -> {
            try {
                setPropertyValue(htmlInputElement.checked);
            } catch (Throwable t) {
                exceptionHandler.handleException("Cannot set property value for property: " + getAbstractPropertyModel(), t);
            }
        }, false);
    }

    @Override
    public void showValue() {
        htmlInputElement.checked = getPropertyValue(false);
    }


    @Override
    public HTMLElement getElement() {
        return htmlInputElement;
    }
}
