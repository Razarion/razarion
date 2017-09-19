package com.btxtech.client.editor.widgets.itemtype.base;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.databinding.client.BindableProxyFactory;
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
@Templated("BaseItemTypeSelectionDialog.html#base-item-selection-dialog")
public class BaseItemTypeSelectionDialog extends Composite implements ModalDialogContent<Integer> {
    @Inject
    private ItemTypeService itemTypeService;
    @Inject
    @AutoBound
    private DataBinder<List<BaseItemType>> binder;
    @Inject
    @Bound
    @DataField
    @ListContainer("tbody")
    private ListComponent<BaseItemType, BaseItemTypeSelectionEntry> baseItemTypes;
    private ModalDialogPanel<Integer> modalDialogPanel;

    @Override
    public void init(Integer selectedId) {
        DOMUtil.removeAllElementChildren(baseItemTypes.getElement()); // Remove placeholder table row from template.
        baseItemTypes.addComponentCreationHandler(baseItemTypeSelectionEntry -> baseItemTypeSelectionEntry.setBaseItemTypeSelectionDialog(BaseItemTypeSelectionDialog.this));
        binder.setModel(new ArrayList<>(itemTypeService.getBaseItemTypes()));
        baseItemTypes.setSelector(baseItemTypeSelectionEntry -> baseItemTypeSelectionEntry.setSelected(true));
        baseItemTypes.setDeselector(baseItemTypeSelectionEntry -> baseItemTypeSelectionEntry.setSelected(false));
        if (selectedId != null) {
            // Problem whit Errai binder proxy end equals
            baseItemTypes.selectModel(BindableProxyFactory.getBindableProxy(itemTypeService.getBaseItemType(selectedId)));
        }
    }

    @Override
    public void customize(ModalDialogPanel<Integer> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void selectComponent(BaseItemTypeSelectionEntry widget) {
        baseItemTypes.deselectAll();
        baseItemTypes.selectComponent(widget);
        modalDialogPanel.setApplyValue(widget.getValue().getId());
    }

    @Override
    public void onClose() {

    }
}
