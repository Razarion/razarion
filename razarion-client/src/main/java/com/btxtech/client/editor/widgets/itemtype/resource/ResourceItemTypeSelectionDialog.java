package com.btxtech.client.editor.widgets.itemtype.resource;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("ResourceItemTypeSelectionDialog.html#resource-item-selection-dialog")
public class ResourceItemTypeSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    @AutoBound
    private DataBinder<List<ResourceItemType>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<ResourceItemType, ResourceItemTypeSelectionEntry> resourceItemTypes;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(resourceItemTypes.getElement()); // Remove placeholder table row from template.
        resourceItemTypes.addComponentCreationHandler(resourceItemTypeSelectionEntry -> resourceItemTypeSelectionEntry.setResourceItemTypeSelectionDialog(ResourceItemTypeSelectionDialog.this));
        binder.setModel(new ArrayList<>(itemTypeService.getResourceItemTypes()));
        resourceItemTypes.setSelector(resourceItemTypeSelectionEntry -> resourceItemTypeSelectionEntry.setSelected(true));
        resourceItemTypes.setDeselector(resourceItemTypeSelectionEntry -> resourceItemTypeSelectionEntry.setSelected(false));
        if (selectedId != null) {
            resourceItemTypes.selectModel(itemTypeService.getResourceItemType(selectedId));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(ResourceItemTypeSelectionEntry widget) {
        resourceItemTypes.deselectAll();
        resourceItemTypes.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getId());
    }

    @Override
    public void onClose() {

    }
}
