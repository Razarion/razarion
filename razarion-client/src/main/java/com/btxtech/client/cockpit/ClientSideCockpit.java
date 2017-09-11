package com.btxtech.client.cockpit;

import com.btxtech.client.cockpit.radar.RadarPanel;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.inventory.InventoryDialog;
import com.btxtech.client.editor.EditorMenuDialog;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.uiservice.cockpit.SideCockpit;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RootPanel;
import org.jboss.errai.common.client.dom.Div;
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
    private GameUiControl gameUiControl;
    @Inject
    private UserUiService userUiService;
    @Inject
    @DataField
    private TableRow editorTableRow;
    @Inject
    @DataField
    private Button editorButton;
    @Inject
    @DataField
    private Button inventoryButton;
    @Inject
    @DataField
    private Button scrollHomeButton;
    @Inject
    @DataField
    private Button fullScreenButton;
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
    @Inject
    @DataField
    private EnergyBarWidget energyBar;
    @Inject
    @DataField
    private TableRow radarPanelTableRow;
    @Inject
    @DataField
    private RadarPanel radarPanel;
    @Inject
    @DataField
    private Div radarNoEnergyDiv;
    @Inject
    @DataField
    private Div radarNoEnergyInnerDiv;

    @PostConstruct
    public void init() {
        getElement().getStyle().setZIndex(ZIndexConstants.MAIN_COCKPIT);
        GwtUtils.preventContextMenu(this);
    }

    @Override
    public void show() {
        RootPanel.get().add(this);
        editorTableRow.getStyle().setProperty("display", userUiService.isAdmin() ? "table-row" : "none");
    }

    @Override
    public void hide() {
        RootPanel.get().remove(this);
    }

    @EventHandler("inventoryButton")
    private void onInventoryButtonClick(ClickEvent event) {
        modalDialogManager.show("Inventory", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryDialog.class, null, null, (modalDialogPanel) -> gameTipService.onInventoryDialogOpened(), DialogButton.Button.CLOSE);
    }

    @EventHandler("editorButton")
    private void onEditorButtonClick(ClickEvent event) {
        modalDialogManager.show("Editor Menu", ClientModalDialogManagerImpl.Type.QUEUE_ABLE, EditorMenuDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("scrollHomeButton")
    private void onScrollHomeButtonClick(ClickEvent event) {
        gameUiControl.scrollToHome();
    }

    @EventHandler("fullScreenButton")
    private void onFullScreenButtonClick(ClickEvent event) {
        GwtUtils.toggleFullscreen(RootPanel.get().getElement());
    }

    @Override
    public void displayResources(int resources) {
        resourceLabel.setTextContent(Integer.toString(resources));
    }

    @Override
    public void displayItemCount(int itemCount, int houseSpace) {
        itemCountLabel.setTextContent(itemCount + " / " + houseSpace);
    }

    @Override
    public void displayEnergy(int consuming, int generating) {
        energyBar.setEnergy(consuming, generating);
    }

    @Override
    public void displayXps(int xp, int xp2LevelUp) {
        xpLabel.setTextContent(xp + " / " + xp2LevelUp);
    }

    @Override
    public void displayLevel(int levelNumber) {
        levelLabel.setTextContent(Integer.toString(levelNumber));
    }

    @Override
    public Rectangle getInventoryDialogButtonLocation() {
        return new Rectangle(inventoryButton.getAbsoluteLeft(), inventoryButton.getAbsoluteTop(), inventoryButton.getOffsetWidth(), inventoryButton.getOffsetHeight());
    }

    @Override
    public Rectangle getScrollHomeButtonLocation() {
        return new Rectangle(scrollHomeButton.getAbsoluteLeft(), scrollHomeButton.getAbsoluteTop(), scrollHomeButton.getOffsetWidth(), scrollHomeButton.getOffsetHeight());
    }

    @Override
    public void showRadar(GameUiControl.RadarState radarState) {
        switch (radarState) {
            case NONE:
                radarPanelTableRow.getStyle().setProperty("display", "none");
                radarNoEnergyDiv.getStyle().setProperty("display", "none");
                radarPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
                radarPanel.stop();
                break;
            case NO_POWER:
                radarPanelTableRow.getStyle().setProperty("display", "table-row");
                radarNoEnergyDiv.getStyle().setProperty("display", "table");
                radarNoEnergyInnerDiv.getStyle().setProperty("width", RadarPanel.WIDTH + "px");
                radarNoEnergyInnerDiv.getStyle().setProperty("height", RadarPanel.HEIGHT + "px");
                radarNoEnergyInnerDiv.setInnerHTML(I18nHelper.getConstants().radarNoPower());
                radarPanel.getElement().getStyle().setDisplay(Style.Display.NONE);
                radarPanel.stop();
                break;
            case WORKING:
                radarPanelTableRow.getStyle().setProperty("display", "table-row");
                radarNoEnergyDiv.getStyle().setProperty("display", "none");
                radarPanel.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                radarPanel.show();
                break;
            default:
                throw new IllegalArgumentException("ClientSideCockpit.showRadar() Unknown radarState: " + radarState);
        }
    }

    @Override
    public void clean() {
        radarPanel.stop();
    }
}
