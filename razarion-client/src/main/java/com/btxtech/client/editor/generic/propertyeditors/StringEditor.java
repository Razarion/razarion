package com.btxtech.client.editor.generic.propertyeditors;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLInputElement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

@Dependent
public class StringEditor extends AbstractPropertyEditor<String> {
    private HTMLInputElement htmlInputElement;

    @PostConstruct
    public void postConstruct() {
        htmlInputElement = (HTMLInputElement) DomGlobal.document.createElement("input");
    }

    @Override
    public void showValue() {
        htmlInputElement.value = getPropertyValueString();
        htmlInputElement.addEventListener("input", event -> setPropertyValue(htmlInputElement.value), false);
    }

    @Override
    public HTMLElement getElement() {
        return htmlInputElement;
    }
}
