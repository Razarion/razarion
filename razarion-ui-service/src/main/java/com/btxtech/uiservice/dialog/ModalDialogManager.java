package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

/**
 * Created by Beat
 * 24.09.2016.
 */
public interface ModalDialogManager {
    void showQuestPassed(QuestConfig questConfig, ApplyListener<QuestConfig> applyListener);

    void showLevelUp(UserContext userContext, ApplyListener<QuestConfig> applyListener);
}
