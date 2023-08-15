package com.btxtech.client.cockpit.quest;

import com.btxtech.client.MainPanelService;
import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotSceneIndicationInfo;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.shared.system.ExceptionHandler;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Deprecated
@Singleton
public class QuestCockpitImpl /* implements QuestCockpit*/ {
    // private Logger logger = Logger.getLogger(TopRightCockpitImpl.class.getName());
    @Inject
    private Instance<TopRightCockpitWidget> instance;
    @Inject
    private MainPanelService mainPanelService;
    @Inject
    private ExceptionHandler exceptionHandler;
    private TopRightCockpitWidget topRightCockpitWidget;

    // TODO @Override
    public void showQuestSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        try {
            if (descriptionConfig != null || showQuestSelectionButton) {
                getTopRightCockpitWidget().getQuestSidebar().show(true);
                getTopRightCockpitWidget().getQuestSidebar().setQuest(descriptionConfig, questProgressInfo, showQuestSelectionButton);
            } else {
                getTopRightCockpitWidget().getQuestSidebar().show(false);
            }
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    // TODO @Override
    public void setShowQuestInGameVisualisation(boolean showInGameVisualisation) {
        // TODO
    }

    // TODO @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        try {
            getTopRightCockpitWidget().getQuestSidebar().onQuestProgress(questProgressInfo);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    // TODO @Override
    public void setBotSceneIndicationInfos(List<BotSceneIndicationInfo> botSceneIndicationInfos) {
        try {
            getTopRightCockpitWidget().setBotSceneIndicationInfos(botSceneIndicationInfos);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    private TopRightCockpitWidget getTopRightCockpitWidget() {
        if (topRightCockpitWidget == null) {
            topRightCockpitWidget = instance.get();
            mainPanelService.addToGamePanel(topRightCockpitWidget);
        }
        return topRightCockpitWidget;
    }
}
