package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * 11.07.2016.
 */
// TODO remove com.btxtech.client.cockpit.quest.TopRightCockpitImpl
@Singleton // TODO Rename to QuestCockpit
public class TopRightCockpit {
    public void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        // TODO
    }

    public void setShowQuestInGameVisualisation(boolean showInGameVisualisation) {
        // TODO
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        // TODO
    }

    @Deprecated
    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        // TODO
    }
}
