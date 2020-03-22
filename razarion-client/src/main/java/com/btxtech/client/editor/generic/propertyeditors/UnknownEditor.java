package com.btxtech.client.editor.generic.propertyeditors;

import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

@Dependent
public class UnknownEditor extends AbstractPropertyEditor {
    private HTMLDivElement htmldivElement;

    @PostConstruct
    public void postConstruct() {
        htmldivElement = (HTMLDivElement) DomGlobal.document.createElement("div");
    }

    @Override
    public void showValue() {
        htmldivElement.textContent = "Unknown editor: " + getPropertyModel();
    }

    @Override
    public HTMLElement getElement() {
        return htmldivElement;
    }
}
