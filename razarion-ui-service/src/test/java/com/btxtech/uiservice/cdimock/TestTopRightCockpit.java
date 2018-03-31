package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.cockpit.TopRightCockpit;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestTopRightCockpit implements TopRightCockpit {
    @Override
    public void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShowQuestInGameVisualisation(boolean showInGameVisualisation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        throw new UnsupportedOperationException();
    }
}
