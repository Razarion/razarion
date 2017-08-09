package com.btxtech.client.cockpit.quest;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Singleton
public class ClientQuestVisualizerImpl implements QuestVisualizer {
    private Logger logger = Logger.getLogger(ClientQuestVisualizerImpl.class.getName());
    @Inject
    private Instance<QuestSidebar> questCockpitInstance;
    private QuestSidebar questSidebar;

    @Override
    public void showSideBar(QuestDescriptionConfig descriptionConfig) {
        if (descriptionConfig != null) {
            if (questSidebar == null) {
                questSidebar = questCockpitInstance.get();
                RootPanel.get().add(questSidebar);
            }
            questSidebar.setQuest(descriptionConfig);
        } else {
            if (questSidebar != null) {
                RootPanel.get().remove(questSidebar);
                questSidebar = null;
            }
        }
    }

    @Override
    public void setShowInGameVisualisation(boolean showInGameVisualisation) {
        // TODO
    }

    @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        if(questSidebar != null) {
            questSidebar.onQuestProgress(questProgressInfo);
        } else {
            logger.severe("ClientQuestVisualizerImpl.onQuestProgress() questSidebar == null");
        }
    }
}
