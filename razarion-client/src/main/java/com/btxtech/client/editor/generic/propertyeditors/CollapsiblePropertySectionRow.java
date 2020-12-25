package com.btxtech.client.editor.generic.propertyeditors;

import com.btxtech.client.editor.generic.model.AbstractPropertyModel;
import com.btxtech.client.utils.Elemental2Utils;
import com.btxtech.shared.system.ExceptionHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import elemental2.dom.HTMLButtonElement;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLTableRowElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.logging.Logger;

@Templated("CollapsiblePropertySectionRow.html#propertyTableRow")
public class CollapsiblePropertySectionRow implements IsElement {
    private Logger logger = Logger.getLogger(CollapsiblePropertySectionRow.class.getName());
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<AbstractPropertyEditor> propertyEditorInstance;
    @Inject
    @DataField
    private HTMLTableRowElement propertyTableRow;
    @Inject
    @DataField
    private HTMLDivElement propertyName;
    @Inject
    @DataField
    private HTMLButtonElement collapseButton;
    @Inject
    @DataField
    private HTMLButtonElement createDeleteButton;
    @Inject
    @DataField
    private HTMLDivElement propertyEditorDiv;
    private AbstractPropertyModel abstractPropertyModel;
    private ListEditor childListEditor;
    private boolean showPropertyEditorDiv = true;

    public void init(AbstractPropertyModel abstractPropertyModel) {
        this.abstractPropertyModel = abstractPropertyModel;
        propertyName.textContent = abstractPropertyModel.getDisplayName();
        display();
        displayCollapseState();
    }

    @EventHandler("collapseButton")
    private void onCollapseButtonClicked(ClickEvent event) {
        showPropertyEditorDiv = !showPropertyEditorDiv;
        displayCollapseState();
    }

    @EventHandler("createDeleteButton")
    private void onCreateDeleteButtonClicked(ClickEvent event) {
        if (childListEditor == null && !abstractPropertyModel.isPropertyNullable()) {
            logger.warning("CollapsiblePropertySectionRow.onCreateDeleteButtonClicked() invalid state: " + abstractPropertyModel);
            return;
        }

        if (abstractPropertyModel.isPropertyValueNotNull()) {
            if (childListEditor != null) {
                childListEditor.createChildElement();
            } else {
                abstractPropertyModel.setPropertyValue(null);
            }
        } else {
            abstractPropertyModel.createAndSetPropertyValue();
            display();
            if (childListEditor != null) {
                childListEditor.createChildElement();
            }
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
                AbstractPropertyEditor<?> abstractPropertyEditor = propertyEditorInstance.select(abstractPropertyModel.getEditorClass()).get();
                if (abstractPropertyEditor instanceof ListEditor) {
                    childListEditor = (ListEditor) abstractPropertyEditor;
                } else {
                    childListEditor = null;
                }
                abstractPropertyEditor.init(abstractPropertyModel);
                propertyEditorDiv.appendChild(abstractPropertyEditor.getElement());
            }
        } catch (Throwable t) {
            propertyEditorDiv.textContent = t.toString();
            exceptionHandler.handleException(t);
        }
        // Button
        try {
            if (abstractPropertyModel.isPropertyNullable() || childListEditor != null) {
                createDeleteButton.style.display = "inline-block";
                if (childListEditor != null) {
                    createDeleteButton.textContent = "Create";
                } else if (abstractPropertyModel.isPropertyValueNotNull()) {
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

    private void displayCollapseState() {
        if (showPropertyEditorDiv) {
            propertyEditorDiv.style.display = "block";
            collapseButton.textContent = "^";
        } else {
            propertyEditorDiv.style.display = "none";
            collapseButton.textContent = "v";
        }
    }
}
