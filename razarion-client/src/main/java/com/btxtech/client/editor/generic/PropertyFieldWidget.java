package com.btxtech.client.editor.generic;

import com.google.gwt.user.client.TakesValue;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import jsinterop.base.Js;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

@Templated("GenericPropertyPanel.html#genericPropertyPanel")
public class PropertyFieldWidget implements IsElement, TakesValue<PropertyField> {
    @Inject
    @DataField
    private HTMLTableRowElement propertyTableRow;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyName;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyValue;
    private PropertyField propertyField;

    @Override
    public HTMLElement getElement() {
        return Js.cast(propertyTableRow);
    }

    @Override
    public void setValue(PropertyField propertyField) {
        this.propertyField = propertyField;
        propertyName.textContent = propertyField.getPropertyName();
        propertyValue.textContent = propertyField.getValue();
    }

    @Override
    public PropertyField getValue() {
        return propertyField;
    }
}
