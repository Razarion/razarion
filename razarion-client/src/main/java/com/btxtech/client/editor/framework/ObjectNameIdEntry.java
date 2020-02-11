package com.btxtech.client.editor.framework;

import com.btxtech.common.DisplayUtils;
import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;

/**
 * Created by Beat
 * 23.08.2016.
 */
@Templated("ObjectNameIdTable.html#objectNameIdRow")
public class ObjectNameIdEntry implements TakesValue<ObjectNameId>, IsElement {
    @Inject
    @DataField
    private TableRow objectNameIdRow;
    @Inject
    @DataField
    private Label id;
    @Inject
    @DataField
    private Label internalName;
    @Inject
    @DataField
    private Button editButton;
    @Inject
    @DataField
    private Button upButton;
    @Inject
    @DataField
    private Button downButton;
    @Inject
    @DataField
    private Button deleteButton;
    private ObjectNameId objectNameId;
    private AbstractObjectNameIdEditor abstractObjectNameIdEditor;

    @Override
    public void setValue(ObjectNameId objectNameId) {
        this.objectNameId = objectNameId;
        id.setText(DisplayUtils.handleInteger(objectNameId.getId()));
        internalName.setText(objectNameId.getInternalName());
    }

    @Override
    public ObjectNameId getValue() {
        return objectNameId;
    }

    @Override
    public HTMLElement getElement() {
        return objectNameIdRow;
    }

    @EventHandler("upButton")
    public void upButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.up(objectNameId, response -> abstractObjectNameIdEditor.read(abstractObjectNameIdEditor::setObjectNameIds));
    }

    @EventHandler("downButton")
    public void downButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.down(objectNameId, response -> abstractObjectNameIdEditor.read(abstractObjectNameIdEditor::setObjectNameIds));
    }

    @EventHandler("deleteButton")
    public void deleteButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.delete(objectNameId, response -> abstractObjectNameIdEditor.read(abstractObjectNameIdEditor::setObjectNameIds));
    }

    @EventHandler("editButton")
    public void editButtonClicked(ClickEvent event) {
        // TODO ??? ObjectNamePropertyPanel objectNamePropertyPanel = leftSideBarManager.stack(abstractObjectNameIdEditor.getObjectNamePropertyPanelClass());
        // TODO ??? objectNamePropertyPanel.setObjectNameId(objectNameId);
    }

    public void setAbstractObjectNameIdEditor(AbstractObjectNameIdEditor abstractObjectNameIdEditor) {
        this.abstractObjectNameIdEditor = abstractObjectNameIdEditor;
        upButton.setEnabled(abstractObjectNameIdEditor.hasPredecessor(objectNameId));
        downButton.setEnabled(abstractObjectNameIdEditor.hasSuccessor(objectNameId));
    }
}
