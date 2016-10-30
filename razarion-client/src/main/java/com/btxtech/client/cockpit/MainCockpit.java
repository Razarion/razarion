package com.btxtech.client.cockpit;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.inventory.InventoryDialog;
import com.btxtech.client.editor.EditorMenuDialog;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 12.08.2016.
 */
@Templated("MainCockpit.html#cockpit")
public class MainCockpit extends Composite {
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button inventoryButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button editorButton;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.MAIN_COCKPIT);
    }

    @EventHandler("inventoryButton")
    private void onInventoryButtonClick(ClickEvent event) {
        modalDialogManager.show("Inventory", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryDialog.class, null, null);
    }

    @EventHandler("editorButton")
    private void onEditorButtonClick(ClickEvent event) {
        modalDialogManager.show("Editor Menu", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, EditorMenuDialog.class, null, null);
    }
}
