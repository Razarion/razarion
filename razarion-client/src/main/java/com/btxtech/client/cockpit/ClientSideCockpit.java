package com.btxtech.client.cockpit;

import com.btxtech.client.MainPanelService;
import com.btxtech.client.cockpit.radar.RadarPanel;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.inventory.InventoryDialog;
import com.btxtech.client.dialog.unlock.UnlockDialog;
import com.btxtech.client.editor.EditorService;
import com.btxtech.client.utils.GwtUtils;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.dialog.DialogButton;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.tip.GameTipService;
import com.btxtech.uiservice.unlock.UnlockUiService;
import com.btxtech.uiservice.user.UserUiService;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Button;
import elemental2.dom.HTMLDivElement;
import elemental2.dom.HTMLElement;
import org.jboss.errai.common.client.api.elemental2.IsElement;
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
@Deprecated
public class ClientSideCockpit implements IsElement {
    @Inject
    private GameTipService gameTipService;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private UserUiService userUiService;
    @Inject
    private UnlockUiService unlockUiService;
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private EditorService editorService;
    @Inject
    @DataField
    private HTMLDivElement cockpit;
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
    @DataField
    private Button unlockButton;
    @Inject
    @DataField
    private Button userAccountButton;
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
    @Inject
    @DataField
    private TableRow registerTr;
    @Inject
    @DataField
    private Button registerButton;
    @Inject
    @DataField
    private TableRow setNameTr;
    @Inject
    @DataField
    private Button setNameButton;

    @PostConstruct
    public void init() {
        cockpit.style.zIndex = ZIndexConstants.MAIN_COCKPIT;
        GwtUtils.preventContextMenu(cockpit);
        unlockUiService.setBlinkListener(blink -> {
            if (blink) {
                //noinspection GWTStyleCheck
                unlockButton.addStyleName("button-blink");
            } else {
                //noinspection GWTStyleCheck
                unlockButton.removeStyleName("button-blink");
            }
        });
        userUiService.setUserRegistrationListener(this::displayUserRegistration);
    }

    @Override
    public HTMLElement getElement() {
        return cockpit;
    }

    // TODO @Override
    public void show() {
        mainPanelService.addToGamePanel(this);
        editorTableRow.getStyle().setProperty("display", userUiService.isAdmin() ? "table-row" : "none");
    }

    // TODO @Override
    public void hide() {
        mainPanelService.removeFromGamePanel(this);
    }

    @EventHandler("inventoryButton")
    private void onInventoryButtonClick(ClickEvent event) {
        modalDialogManager.show(I18nHelper.getConstants().inventory(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, InventoryDialog.class, null, null, (modalDialogPanel) -> gameTipService.onInventoryDialogOpened(), DialogButton.Button.CLOSE);
    }

    @EventHandler("editorButton")
    private void onEditorButtonClick(ClickEvent event) {
        editorService.openEditorMenu();
    }

    @EventHandler("scrollHomeButton")
    private void onScrollHomeButtonClick(ClickEvent event) {
        gameUiControl.scrollToHome();
    }

    @EventHandler("fullScreenButton")
    private void onFullScreenButtonClick(ClickEvent event) {
        GwtUtils.toggleFullscreen(mainPanelService.getMainPanelElement());
    }

    @EventHandler("unlockButton")
    private void onUnlockButtonClick(ClickEvent event) {
        modalDialogManager.show(I18nHelper.getConstants().unlockDialogTitle(), ClientModalDialogManagerImpl.Type.QUEUE_ABLE, UnlockDialog.class, null, null, null, DialogButton.Button.CLOSE);
    }

    @EventHandler("registerButton")
    private void onRegisterButtonClick(ClickEvent event) {
        modalDialogManager.showRegisterDialog();
    }

    @EventHandler("setNameButton")
    private void onSetNameButtonClick(ClickEvent event) {
        modalDialogManager.showSetUserNameDialog();
    }

    @EventHandler("userAccountButton")
    private void onUserAccountButtonClick(ClickEvent event) {
        modalDialogManager.showUserAccountDialog();
    }

    // TODO @Override
    public void displayResources(int resources) {
        resourceLabel.setTextContent(Integer.toString(resources));
    }

    // TODO @Override
    public void displayItemCount(int itemCount, int houseSpace) {
        itemCountLabel.setTextContent(itemCount + " / " + houseSpace);
    }

    // TODO @Override
    public void displayEnergy(int consuming, int generating) {
        energyBar.setEnergy(consuming, generating);
    }

    // TODO @Override
    public void displayXps(int xp, int xp2LevelUp) {
        xpLabel.setTextContent(xp + " / " + xp2LevelUp);
    }

    // TODO @Override
    public void displayLevel(int levelNumber) {
        levelLabel.setTextContent(Integer.toString(levelNumber));
    }

    // TODO @Override
    public Rectangle getInventoryDialogButtonLocation() {
        return new Rectangle(inventoryButton.getAbsoluteLeft(), inventoryButton.getAbsoluteTop(), inventoryButton.getOffsetWidth(), inventoryButton.getOffsetHeight());
    }

    // TODO @Override
    public Rectangle getScrollHomeButtonLocation() {
        return new Rectangle(scrollHomeButton.getAbsoluteLeft(), scrollHomeButton.getAbsoluteTop(), scrollHomeButton.getOffsetWidth(), scrollHomeButton.getOffsetHeight());
    }

    private void displayUserRegistration(UserContext userContext) {
        registerTr.getStyle().setProperty("display", "none");
        setNameTr.getStyle().setProperty("display", "none");
        userAccountButton.getElement().getStyle().setDisplay(Style.Display.NONE);
        if (!userUiService.isRegistered()) {
            registerTr.getStyle().setProperty("display", "table-row");
            registerButton.setText(I18nHelper.getConstants().register());
        } else if (!userUiService.isRegisteredAndNamed()) {
            setNameTr.getStyle().setProperty("display", "table-row");
            setNameButton.setText(I18nHelper.getConstants().setName());
            userAccountButton.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        } else {
            userAccountButton.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);
        }
    }

    // TODO @Override
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

    // TODO @Override
    public void clean() {
        radarPanel.stop();
    }
}
