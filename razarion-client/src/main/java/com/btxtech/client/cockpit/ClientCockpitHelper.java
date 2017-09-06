package com.btxtech.client.cockpit;

import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.uiservice.Group;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.i18n.I18nHelper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * on 06.09.2017.
 */
@Singleton
public class ClientCockpitHelper {
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private GameEngineControl gameEngineControl;
    @Inject
    private SelectionHandler selectionHandler;

    public void sell() {
        Group group = selectionHandler.getOwnSelection();
        if(group == null) {
            return;
        }
        modalDialogManager.showQuestionDialog(I18nHelper.getConstants().sellConfirmationTitle(), I18nHelper.getConstants().sellConfirmation(), () -> {
            gameEngineControl.sellItems(group.getItems());
        }, null);
    }
}
