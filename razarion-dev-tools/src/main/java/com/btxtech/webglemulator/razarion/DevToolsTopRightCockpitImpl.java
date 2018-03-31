package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.cockpit.TopRightCockpit;

import java.util.List;

/**
 * Created by Beat
 * 11.07.2016.
 */
public class DevToolsTopRightCockpitImpl implements TopRightCockpit {

    @Override
    public void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        System.out.println("++++ DevToolsTopRightCockpitImpl.showQuestSideBar(): " + descriptionConfig);
    }

    @Override
    public void setShowQuestInGameVisualisation(boolean showInGameVisualisation) {
        // TODO
    }

    @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        System.out.println("++++ DevToolsTopRightCockpitImpl.onQuestProgress(): " + questProgressInfo);
    }

    @Override
    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        System.out.println("++++ DevToolsTopRightCockpitImpl.setBotSceneIndicationInfos(): " + botSceneIndicationInfos);
    }
}
