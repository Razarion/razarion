package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.PropertyModel;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

@Templated("ListEditorEntry.html#listEditorEntry")
public class ListEditorEntry implements IsElement {
    @Inject
    private Instance<AbstractPropertyEditor> propertyEditorInstance;
    @Inject
    @DataField
    private HTMLTableRowElement listEditorEntry;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyId;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyName;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyEditor;

    public void init(PropertyModel propertyModel) {
//        if (sceneConfig.getId() != null) {
//            propertyId.textContent = Integer.toString(sceneConfig.getId());
//        }
//        propertyName.textContent = sceneConfig.getInternalName();
        AbstractPropertyEditor abstractPropertyEditor = propertyEditorInstance.select(propertyModel.getEditorClass()).get();
        abstractPropertyEditor.init(propertyModel);
        propertyEditor.appendChild(abstractPropertyEditor.getElement());
    }

    @Override
    public HTMLElement getElement() {
        return listEditorEntry;
    }
}
