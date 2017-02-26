package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.dialog.ModalDialogManager;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestModalDialogManager extends ModalDialogManager {
    @Override
    public void showBoxPicked(BoxContent boxContent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showUseInventoryItemLimitExceeded(BaseItemType baseItemType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showUseInventoryHouseSpaceExceeded() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, Runnable closeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void showLevelUp(LevelConfig newLevelConfig, Runnable closeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void showBaseLost(Runnable closeListener) {
        throw new UnsupportedOperationException();
    }
}
