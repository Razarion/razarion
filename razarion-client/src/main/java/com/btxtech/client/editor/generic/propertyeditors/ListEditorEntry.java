package com.btxtech.client.editor.generic.propertyeditors;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.errai.common.client.api.elemental2.IsElement;

import javax.inject.Inject;

@Templated("ListEditorEntry.html#listEditorEntry")
public class ListEditorEntry implements IsElement{
    @Inject
    @DataField
    private HTMLTableRowElement listEditorEntry;

    @Override
    public HTMLElement getElement() {
        return listEditorEntry;
    }
}
