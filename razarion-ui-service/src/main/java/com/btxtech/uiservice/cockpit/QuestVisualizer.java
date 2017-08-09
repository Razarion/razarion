package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * Created by Beat
 * 11.07.2016.
 */
public interface QuestVisualizer {
    void showSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo);

    void setShowInGameVisualisation(boolean showInGameVisualisation);

    void onQuestProgress(QuestProgressInfo questProgressInfo);
}
