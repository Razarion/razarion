package com.btxtech.client.editor.generic;

import com.btxtech.client.utils.Elemental2Utils;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.databinding.client.HasProperties;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Templated("PropertyPage.html#div")
public class PropertyPage implements IsElement {
    @Inject
    @DataField
    private HTMLDivElement div;
    @Inject
    @DataField
    private HTMLTableElement propertyTable;
    @Inject
    private Instance<PrimitivePropertyWidget> primitivePropertyWidgets;
    @Inject
    private Instance<ComplexPropertyWrapper> complexPropertyWrappers;

    public void init(HasProperties hasProperties) {
        hasProperties.getBeanProperties().forEach((propertyName, propertyType) -> {
            if (PropertyTypeUtils.isPrimitiveProperty(propertyType)) {
                PrimitivePropertyWidget primitivePropertyWidget = primitivePropertyWidgets.get();
                primitivePropertyWidget.init(propertyName, propertyType, hasProperties);
                propertyTable.appendChild(primitivePropertyWidget.getElement());
            } else {
                ComplexPropertyWrapper complexPropertyWrapper = complexPropertyWrappers.get();
                complexPropertyWrapper.init(propertyName, propertyType, hasProperties);
                propertyTable.appendChild(complexPropertyWrapper.getElement());
            }
        });
    }

    public void clean() {
        Elemental2Utils.removeAllChildren(propertyTable);
    }

    @Override
    public HTMLElement getElement() {
        return div;
    }
}
