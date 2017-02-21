package com.btxtech.client.cockpit;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.inventory.InventoryDialog;
import com.btxtech.client.editor.EditorMenuDialog;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.SideCockpit;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.common.client.dom.TableRow;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 12.08.2016.
 */
@Templated("ClientSideCockpit.html#cockpit")
public class ClientSideCockpit extends Composite implements SideCockpit {
    @Inject
    private GameTipService gameTipService;
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private Button inventoryButton;
    @Inject
    @DataField
    private TableRow editorTableRow;
    @Inject
    @DataField
    private Button editorButton;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    @DataField
    private Span resourceLabel;
    @Inject
    @DataField
    private Span itemCountLabel;
    @Inject
    @DataField
    private Span xpLabel;
    @Inject
    @DataField
    private Span levelLabel;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.MAIN_COCKPIT);
        GwtUtils.preventContextMenu(this);
        if(userUiService.isAdmin()) {

        }
    }

    @Override
    public void show() {
        RootPanel.get().add(this);
    }

    @EventHandler("inventoryButton")
    private void onInventoryButtonClick(ClickEvent event) {
        modalDialogManager.show("Inventory", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryDialog.class, null, null, () -> gameTipService.onInventoryDialogOpened(), DialogButton.Button.CLOSE);
    }

    @EventHandler("editorButton")
    private void onEditorButtonClick(ClickEvent event) {
        modalDialogManager.show("Editor Menu", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, EditorMenuDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void displayResources(int resources) {
        resourceLabel.setTextContent(Integer.toString(resources));
    }

    @Override
    public void displayItemCount(int itemCount) {
        itemCountLabel.setTextContent(Integer.toString(itemCount));
    }

    @Override
    public void displayXps(int xp) {
        xpLabel.setTextContent(Integer.toString(xp));
    }

    @Override
    public void displayLevel(int levelNumber) {
        levelLabel.setTextContent(Integer.toString(levelNumber));
    }

    @Override
    public Rectangle getInventoryDialogButtonLocation() {
        return new Rectangle(inventoryButton.getAbsoluteLeft(), inventoryButton.getAbsoluteTop(), inventoryButton.getOffsetWidth(), inventoryButton.getOffsetHeight());
    }
}
