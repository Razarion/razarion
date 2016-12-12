package com.btxtech.client.cockpit;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.inventory.InventoryDialog;
import com.btxtech.client.editor.EditorMenuDialog;
import com.btxtech.uiservice.cockpit.SideCockpit;
import com.btxtech.uiservice.dialog.DialogButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.dom.Span;
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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button inventoryButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Button editorButton;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Span resourceLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Span xpLabel;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    @DataField
    private Span levelLabel;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.MAIN_COCKPIT);
    }

    @Override
    public void show() {
        RootPanel.get().add(this);
    }

    @EventHandler("inventoryButton")
    private void onInventoryButtonClick(ClickEvent event) {
        modalDialogManager.show("Inventory", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryDialog.class, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("editorButton")
    private void onEditorButtonClick(ClickEvent event) {
        modalDialogManager.show("Editor Menu", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, EditorMenuDialog.class, null, null, DialogButton.Button.CLOSE);
    }

    @Override
    public void displayResources(int resources) {
        resourceLabel.setTextContent(Integer.toString(resources));
    }

    @Override
    public void displayXps(int xp) {
        xpLabel.setTextContent(Integer.toString(xp));
    }

    @Override
    public void displayLevel(int levelNumber) {
        levelLabel.setTextContent(Integer.toString(levelNumber));
    }
}
