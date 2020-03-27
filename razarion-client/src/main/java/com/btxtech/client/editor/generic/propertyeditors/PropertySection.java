package com.btxtech.client.editor.generic.propertyeditors;

import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Templated("PropertySection.html#propertySection")
public class PropertySection  extends AbstractPropertyEditor {
    @Inject
    @DataField
    private HTMLDivElement propertySection;
    @Inject
    @DataField
    private HTMLTableElement propertyTable;
    @Inject
    private Instance<PropertySectionRow> propertyRowInstance;

    @Override
    protected void showValue() {
        getBranch().createBindableChildren(childPropertyModel -> {
            PropertySectionRow propertyRow = propertyRowInstance.get();
            propertyRow.init(childPropertyModel);
            propertyTable.appendChild(propertyRow.getElement());
        });
    }

    @Override
    public HTMLElement getElement() {
        return propertySection;
    }
}
