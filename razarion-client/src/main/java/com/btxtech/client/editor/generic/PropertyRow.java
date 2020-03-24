package com.btxtech.client.editor.generic;

import com.btxtech.client.editor.generic.propertyeditors.AbstractPropertyEditor;
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

@Templated("PropertyRow.html#propertyTableRow")
public class PropertyRow implements IsElement {
    private Logger logger = Logger.getLogger(PropertyRow.class.getName());
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
    private PropertyModel propertyModel;

    public void init(PropertyModel propertyModel) {
        this.propertyModel = propertyModel;
        propertyName.textContent = propertyModel.getDisplayName();
        display();
    }

    @EventHandler("createDeleteButton")
    private void onCreateButtonClicked(ClickEvent event) {
        if (!propertyModel.isPropertyNullable()) {
            logger.warning("onCreateButtonClicked() on !propertyModel.isPropertyNullable(): " + propertyModel);
            return;
        }
        if (propertyModel.isPropertyValueNotNull()) {
            propertyModel.setPropertyValue(null);
        } else {
            propertyModel.createAndSetPropertyValue();
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
            if (propertyModel.isPropertyValueNotNull() || !propertyModel.isPropertyNullable()) {
                AbstractPropertyEditor abstractPropertyEditor = propertyEditorInstance.select(propertyModel.getEditorClass()).get();
                abstractPropertyEditor.init(propertyModel);
                propertyEditorDiv.appendChild(abstractPropertyEditor.getElement());
            }
        } catch (Throwable t) {
            propertyEditorDiv.textContent = t.toString();
            exceptionHandler.handleException(t);
        }
        // Button
        try {
            if (propertyModel.isPropertyNullable()) {
                createDeleteButton.style.display = "inline-block";
                if (propertyModel.isPropertyValueNotNull()) {
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
