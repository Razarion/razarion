package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.shared.system.ExceptionHandler;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import elemental2.dom.UIEvent;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.ForEvent;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

@Templated("ListEditorEntry.html#listEditorEntry")
public class ListEditorEntry implements IsElement {
    private Logger logger = Logger.getLogger(ListEditorEntry.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<AbstractPropertyEditor> propertyEditorInstance;
    @Inject
    @DataField
    private HTMLTableRowElement listEditorEntry;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyEditor;
    @Inject
    @DataField
    private HTMLButtonElement deleteButton;
    private AbstractPropertyModel abstractPropertyModel;
    private ListEditor listEditor;

    public void init(AbstractPropertyModel abstractPropertyModel, ListEditor listEditor) {
        this.abstractPropertyModel = abstractPropertyModel;
        this.listEditor = listEditor;
        AbstractPropertyEditor abstractPropertyEditor = propertyEditorInstance.select(abstractPropertyModel.getEditorClass()).get();
        abstractPropertyEditor.init(abstractPropertyModel);
        propertyEditor.appendChild(abstractPropertyEditor.getElement());
    }

    @EventHandler("deleteButton")
    private void onCreateButtonClicked(@ForEvent("click") UIEvent e) {
        try {
            abstractPropertyModel.setPropertyValue(null);
            listEditor.display();
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @Override
    public HTMLElement getElement() {
        return listEditorEntry;
    }
}
