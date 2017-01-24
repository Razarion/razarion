package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.BoxContent;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.uiservice.dialog.AbstractModalDialogManager;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestModalDialogManager extends AbstractModalDialogManager {
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
    protected void showLevelUp(UserContext userContext, Runnable closeListener) {
        throw new UnsupportedOperationException();
    }
}
