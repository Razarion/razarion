package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
public interface QuestCockpit {
    void showQuestSideBar(QuestDescriptionConfig<?> descriptionConfig, boolean showQuestSelectionButton);

    void setShowQuestInGameVisualisation();

    void onQuestProgress(QuestProgressInfo questProgressInfo);
}
