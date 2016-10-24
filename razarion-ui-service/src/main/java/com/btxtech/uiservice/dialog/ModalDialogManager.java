package com.btxtech.uiservice.dialog;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;

/**
 * Created by Beat
 * 24.09.2016.
 */
public interface ModalDialogManager {
    void showQuestPassed(QuestDescriptionConfig questDescriptionConfig, ApplyListener<QuestDescriptionConfig> applyListener);

    void showLevelUp(UserContext userContext, ApplyListener<Void> applyListener);
}
