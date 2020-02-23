package com.btxtech.client.editor.generic;

import com.btxtech.client.utils.Elemental2Utils;
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

@Templated("GenericPropertyBook.html#genericPropertyBook")
public class PropertyWidget implements IsElement, TakesValue<PropertyModel> {
    // private Logger logger = Logger.getLogger(PropertyWidget.class.getName());
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
    private PropertyModel propertyModel;

    @Override
    public HTMLElement getElement() {
        return Js.cast(propertyTableRow);
    }

    @Override
    public void setValue(PropertyModel propertyModel) {
        this.propertyModel = propertyModel;
        propertyName.textContent = propertyModel.getPropertyName();
        Elemental2Utils.removeAllChildren(propertyValue);
        propertyValue.appendChild(PropertyTypeUtils.setupPropertyWidget(propertyModel));
    }

    @Override
    public PropertyModel getValue() {
        return propertyModel;
    }
}
