package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

import java.util.List;

/**
 * Created by Beat
 * 11.07.2016.
 */
public interface TopRightCockpit {
    void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton);

    void setShowQuestInGameVisualisation(boolean showInGameVisualisation);

    void onQuestProgress(QuestProgressInfo questProgressInfo);

    void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos);
}
