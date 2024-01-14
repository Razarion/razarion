package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.control.GameUiControl;

import javax.inject.Singleton;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Singleton
public class QuestCockpitService {
    private final Logger logger = Logger.getLogger(GameUiControl.class.getName());
    private QuestCockpit questCockpit;

    public void init(QuestCockpit questCockpit) {
        this.questCockpit = questCockpit;
    }

    public void showQuestSideBar(QuestDescriptionConfig<?> descriptionConfig, boolean showQuestSelectionButton) {
        if (questCockpit != null) {
            questCockpit.showQuestSideBar(descriptionConfig, showQuestSelectionButton);
        } else {
            logger.warning("No questCockpit showQuestSideBar()");
        }
    }

    public void setShowQuestInGameVisualisation(boolean showInGameVisualisation) {
        if (questCockpit != null) {
            questCockpit.setShowQuestInGameVisualisation();
        } else {
            logger.warning("No questCockpit setShowQuestInGameVisualisation()");
        }
    }

    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        if (questCockpit != null) {
            questCockpit.onQuestProgress(questProgressInfo);
        } else {
            logger.warning("No questCockpit onQuestProgress()");
        }
    }

    @Deprecated
    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        if (questCockpit != null) {
            questCockpit.setBotSceneIndicationInfos();
        } else {
            logger.warning("No questCockpit setBotSceneIndicationInfos()");
        }
    }
}
