package com.btxtech.client.editor.framework;

import com.btxtech.client.utils.DisplayUtils;
import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
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
    private Button edit;
    @Inject
    @DataField
    private Button up;
    @Inject
    @DataField
    private Button down;
    @Inject
    @DataField
    private Button delete;
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

    @EventHandler("edit")
    private void editButtonClicked(ClickEvent event) {
        // TODO abstractObjectNameIdEditor.edit(objectNameId);
    }

    @EventHandler("up")
    private void upButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.up(objectNameId);
    }

    @EventHandler("down")
    private void downButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.down(objectNameId);
    }

    @EventHandler("delete")
    private void deleteButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.delete(objectNameId);
    }

    public void setAbstractObjectNameIdEditor(AbstractObjectNameIdEditor abstractObjectNameIdEditor) {
        this.abstractObjectNameIdEditor = abstractObjectNameIdEditor;
        up.setEnabled(abstractObjectNameIdEditor.hasPredecessor(objectNameId));
        down.setEnabled(abstractObjectNameIdEditor.hasSuccessor(objectNameId));
    }
}
