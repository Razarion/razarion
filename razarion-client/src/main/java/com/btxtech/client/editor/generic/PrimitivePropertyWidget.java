package com.btxtech.client.editor.generic;

import com.btxtech.client.utils.Elemental2Utils;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.databinding.client.PropertyType;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import javax.inject.Named;

@Templated("PrimitivePropertyWidget.html#propertyTableRow")
public class PrimitivePropertyWidget implements IsElement {
    // private Logger logger = Logger.getLogger(PrimitivePropertyWidget.class.getName());
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

    public void init(String propertyName, PropertyType propertyValue, HasProperties hasProperties) {
        this.propertyName.textContent = propertyName;
        Elemental2Utils.removeAllChildren(this.propertyValue);
        this.propertyValue.appendChild(PropertyTypeUtils.setupPropertyWidget(propertyName, propertyValue, hasProperties));
    }

    @Override
    public HTMLTableRowElement getElement() {
        return propertyTableRow;
    }
}
