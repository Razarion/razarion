package com.btxtech.client.dialog.unlock;

import com.btxtech.client.dialog.framework.ModalDialogContent;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.shared.rest.InventoryProvider;
import com.btxtech.uiservice.i18n.I18nHelper;
import com.btxtech.uiservice.unlock.UnlockUiService;
import com.google.gwt.user.client.ui.Composite;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.common.client.dom.DOMUtil;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Table;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.databinding.client.components.ListContainer;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import javax.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 14.09.2017.
 */
@Templated("UnlockDialog.html#unlockDialog")
public class UnlockDialog extends Composite implements ModalDialogContent<Void> {
    private Logger logger = Logger.getLogger(UnlockDialog.class.getName());
    @Inject
    private Caller<InventoryProvider> inventoryProvider;
    @Inject
    private UnlockUiService unlockUiService;
    @Inject
    @DataField
    private Table unlockDialogTable;
    @Inject
    @DataField
    private Div unlockDialogText;
    @Inject
    @DataField
    private Div unlockDialogCrystals;
    @Inject
    @DataField
    @ListContainer("tbody")
    private ListComponent<UnlockItemModel, UnlockItemWidget> unlockTable;
    private ModalDialogPanel<Void> modalDialogPanel;

    @Override
    public void init(Void aVoid) {
        unlockDialogCrystals.setTextContent(I18nHelper.getConstants().availableCrystals("<" + I18nHelper.getConstants().loading() + ">"));
        DOMUtil.removeAllElementChildren(unlockTable.getElement()); // Remove placeholder table row from template.
        display();
        unlockUiService.setLevelUnlockListener(this::display);
    }

    @Override
    public void customize(ModalDialogPanel<Void> modalDialogPanel) {
        this.modalDialogPanel = modalDialogPanel;
    }

    public void close() {
        modalDialogPanel.close();
    }

    @Override
    public void onClose() {
        unlockUiService.setLevelUnlockListener(null);
    }

    private void display() {
        if (unlockUiService.hasItems2Unlock()) {
            unlockDialogText.setTextContent(I18nHelper.getConstants().unlockDialogText());
            unlockDialogTable.getStyle().setProperty("display", "table");
            unlockTable.setValue(unlockUiService.getLevelUnlockConfigs().stream().map(levelUnlockConfig -> new UnlockItemModel(levelUnlockConfig, this)).collect(Collectors.toList()));
        } else {
            unlockDialogText.setTextContent(I18nHelper.getConstants().nothingToUnlockDialogText());
            unlockDialogTable.getStyle().setProperty("display", "none");
        }
        inventoryProvider.call((RemoteCallback<Integer>) crystals -> unlockDialogCrystals.setTextContent(I18nHelper.getConstants().availableCrystals(Integer.toString(crystals))), (message, throwable) -> {
            logger.log(Level.SEVERE, "UnlockDialog: InventoryProvider.loadCrystals() failed: message: " + message, throwable);
            return false;
        }).loadCrystals();
    }
}
