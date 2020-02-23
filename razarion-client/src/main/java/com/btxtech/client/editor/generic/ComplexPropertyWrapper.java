package com.btxtech.client.editor.generic;

import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.databinding.client.BindableProxyFactory;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

@Templated("ComplexPropertyWrapper.html#complexPropertyRow")
public class ComplexPropertyWrapper implements IsElement {
    @Inject
    @DataField
    private HTMLTableRowElement complexPropertyRow;
    @Inject
    @DataField
    private HTMLDivElement propertyNameDiv;
    @Inject
    @DataField
    private HTMLButtonElement createDeleteButton;
    @Inject
    @DataField
    private PropertyPage propertyPage;
    private String propertyName;
    private PropertyType propertyType;
    private HasProperties hasProperties;
    private Object complexProperty;

    public void init(String propertyName, PropertyType propertyType, HasProperties hasProperties) {
        this.propertyName = propertyName;
        this.hasProperties = hasProperties;
        this.propertyNameDiv.textContent = propertyName;
        this.propertyType = propertyType;
        complexProperty = hasProperties.get(propertyName);
        if (complexProperty != null) {
            propertyPage.init((HasProperties) BindableProxyFactory.getBindableProxy(complexProperty));
        }
        handleCreateDeleteButton();
    }

    @EventHandler("createDeleteButton")
    private void onCreateButtonClicked(ClickEvent event) {
        if (complexProperty != null) {
            complexProperty = null;
            propertyPage.clean();
        } else {
            complexProperty = BindableProxyFactory.getBindableProxy(propertyType.getType());
            propertyPage.init((HasProperties) BindableProxyFactory.getBindableProxy(complexProperty));
        }
        hasProperties.set(propertyName, complexProperty);
        handleCreateDeleteButton();
    }

    private void handleCreateDeleteButton() {
        if (complexProperty == null) {
            createDeleteButton.textContent = "Create";
        } else {
            createDeleteButton.textContent = "Delete";
        }
    }

    @Override
    public HTMLElement getElement() {
        return complexPropertyRow;
    }
}
