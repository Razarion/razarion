package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLTableCellElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.logging.Logger;

@Templated("PropertySectionRow.html#propertyTableRow")
public class PropertySectionRow implements IsElement {
    private Logger logger = Logger.getLogger(PropertySectionRow.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<AbstractPropertyEditor> propertyEditorInstance;
    @Inject
    @DataField
    private HTMLTableRowElement propertyTableRow;
    @Inject
    @DataField
    @Named("td")
    private HTMLTableCellElement propertyName;
    @Inject
    @DataField
    private HTMLButtonElement createDeleteButton;
    @Inject
    @DataField
    private HTMLDivElement propertyEditorDiv;
    private AbstractPropertyModel abstractPropertyModel;

    public void init(AbstractPropertyModel abstractPropertyModel) {
        this.abstractPropertyModel = abstractPropertyModel;
        propertyName.textContent = abstractPropertyModel.getDisplayName();
        display();
    }

    @EventHandler("createDeleteButton")
    private void onCreateButtonClicked(ClickEvent event) {
        if (!abstractPropertyModel.isPropertyNullable()) {
            logger.warning("onCreateButtonClicked() on !abstractPropertyModel.isPropertyNullable(): " + abstractPropertyModel);
            return;
        }
        if (abstractPropertyModel.isPropertyValueNotNull()) {
            abstractPropertyModel.setPropertyValue(null);
        } else {
            abstractPropertyModel.createAndSetPropertyValue();
        }
        display();
    }


    @Override
    public HTMLTableRowElement getElement() {
        return propertyTableRow;
    }

    private void display() {
        // Value
        Elemental2Utils.removeAllChildren(propertyEditorDiv);
        try {
            if (abstractPropertyModel.isPropertyValueNotNull() || !abstractPropertyModel.isPropertyNullable()) {
                AbstractPropertyEditor abstractPropertyEditor = propertyEditorInstance.select(abstractPropertyModel.getEditorClass()).get();
                abstractPropertyEditor.init(abstractPropertyModel);
                propertyEditorDiv.appendChild(abstractPropertyEditor.getElement());
            }
        } catch (Throwable t) {
            propertyEditorDiv.textContent = t.toString();
            exceptionHandler.handleException(t);
        }
        // Button
        try {
            if (abstractPropertyModel.isPropertyNullable()) {
                createDeleteButton.style.display = "inline-block";
                if (abstractPropertyModel.isPropertyValueNotNull()) {
                    createDeleteButton.textContent = "Delete";
                } else {
                    createDeleteButton.textContent = "Create";
                }
            } else {
                createDeleteButton.style.display = "none";
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }
}
