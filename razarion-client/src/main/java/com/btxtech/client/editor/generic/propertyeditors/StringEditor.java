package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;
import org.jboss.errai.databinding.client.HasProperties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class StringEditor implements GenericPropertyEditor {
    @Inject
    private ExceptionHandler exceptionHandler;
    private HTMLInputElement htmlInputElement;

    @PostConstruct
    public void postConstruct() {
        htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
    }

    @Override
    public void init(String propertyName, HasProperties hasProperties) {
        String value = (String) hasProperties.get(propertyName);
        if (value != null) {
            htmlInputElement.value = value;
        }
        htmlInputElement.addEventListener("input", event -> writeStringValue(propertyName, hasProperties), false);
    }

    private void writeStringValue(String propertyName, HasProperties hasProperties) {
        try {
            hasProperties.set(propertyName, htmlInputElement.textContent);
        } catch (Throwable t) {
            exceptionHandler.handleException("Cannot set property value for property: " + propertyName, t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return htmlInputElement;
    }
}
