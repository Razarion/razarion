package com.btxtech.client.editor.widgets.itemtype.box;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
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
import java.util.logging.Logger;

/**
 * Created by Beat
 * 22.08.2016.
 */
@Templated("BoxItemTypeSelectionDialog.html#box-item-selection-dialog")
public class BoxItemTypeSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    private Logger logger = Logger.getLogger(BoxItemTypeSelectionDialog.class.getName());
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    @AutoBound
    private DataBinder<List<BoxItemType>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<BoxItemType, BoxItemTypeSelectionEntry> boxItemTypes;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(boxItemTypes.getElement()); // Remove placeholder table row from template.
        boxItemTypes.addComponentCreationHandler(boxItemTypeSelectionEntry -> boxItemTypeSelectionEntry.setBoxItemTypeSelectionDialog(BoxItemTypeSelectionDialog.this));
        binder.setModel(new ArrayList<>(itemTypeService.getBoxItemTypes()));
        boxItemTypes.setSelector(boxItemTypeSelectionEntry -> boxItemTypeSelectionEntry.setSelected(true));
        boxItemTypes.setDeselector(boxItemTypeSelectionEntry -> boxItemTypeSelectionEntry.setSelected(false));
        if (selectedId != null) {
            boxItemTypes.selectModel(itemTypeService.getBoxItemType(selectedId));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(BoxItemTypeSelectionEntry widget) {
        boxItemTypes.deselectAll();
        boxItemTypes.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getId());
    }

    @Override
    public void onClose() {

    }
}
