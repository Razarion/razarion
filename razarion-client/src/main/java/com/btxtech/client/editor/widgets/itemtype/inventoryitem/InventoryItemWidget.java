package com.btxtech.client.editor.widgets.itemtype.inventoryitem;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.shared.gameengine.InventoryService;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 07.08.2017.
 */
@Templated("InventoryItemWidget.html#inventoryitemtype")
public class InventoryItemWidget {
    // private Logger logger = Logger.getLogger(InventoryItemWidget.class.getName());
    @Inject
    private InventoryService inventoryService;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Span nameLabel;
    @Inject
    @DataField
    private Button galleryButton;
    private Integer inventoryItemId;
    private Consumer<Integer> changeCallback;

    public void init(Integer inventoryItemId, Consumer<Integer> changeCallback) {
        this.inventoryItemId = inventoryItemId;
        this.changeCallback = changeCallback;
        setupNameLabel();
    }

    @EventHandler("galleryButton")
    private void onGalleryButtonClick(ClickEvent event) {
        modalDialogManager.show("Inventory items", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryItemSelectionDialog.class, inventoryItemId, (button, selectedId) -> {
            if (button == DialogButton.Button.APPLY) {
                inventoryItemId = selectedId;
                changeCallback.accept(inventoryItemId);
                setupNameLabel();
            }
        }, null, DialogButton.Button.CANCEL, DialogButton.Button.APPLY);
    }

    private void setupNameLabel() {
        if (inventoryItemId != null) {
            nameLabel.setInnerHTML(inventoryService.getInventoryItem(inventoryItemId).createObjectNameId().toString());
        } else {
            nameLabel.setInnerHTML("-");
        }
    }

}
