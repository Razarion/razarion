package com.btxtech.client.editor.framework;

import com.btxtech.shared.dto.ObjectNameId;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 23.08.2016.
 */
@Templated("ObjectNameIdTable.html#objectNameDiv")
public class ObjectNameIdTable {
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<ObjectNameId, ObjectNameIdEntry> objectNameIdTable;
    @Inject
    @DataField
    private Button createButton;
    private AbstractObjectNameIdEditor abstractObjectNameIdEditor;

    @PostConstruct
    public void postConstruct() {
        DOMUtil.removeAllElementChildren(objectNameIdTable.getElement()); // Remove placeholder table row from template.
        objectNameIdTable.addComponentCreationHandler(objectNameIdEntry -> objectNameIdEntry.setAbstractObjectNameIdEditor(abstractObjectNameIdEditor));
    }

    public void init(AbstractObjectNameIdEditor abstractObjectNameIdEditor) {
        this.abstractObjectNameIdEditor = abstractObjectNameIdEditor;
        abstractObjectNameIdEditor.setUpdateListener(() -> {
            objectNameIdTable.setValue(abstractObjectNameIdEditor.getObjectNameIds());
        });
        abstractObjectNameIdEditor.read(abstractObjectNameIdEditor::setObjectNameIds);
    }

    @EventHandler("createButton")
    private void createButtonClicked(ClickEvent event) {
        abstractObjectNameIdEditor.create(response -> abstractObjectNameIdEditor.read(abstractObjectNameIdEditor::setObjectNameIds));
    }
}
