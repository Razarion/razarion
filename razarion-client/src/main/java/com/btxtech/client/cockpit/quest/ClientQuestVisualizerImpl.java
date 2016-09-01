package com.btxtech.client.cockpit.quest;

import com.btxtech.uiservice.cockpit.QuestVisualizer;
import com.google.gwt.user.client.ui.RootPanel;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 11.07.2016.
 */
@Singleton
public class ClientQuestVisualizerImpl implements QuestVisualizer {
    @Inject
    private Instance<QuestSidebar> questCockpitInstance;
    private QuestSidebar questSidebar;

    @Override
    public void showSideBar(boolean visible) {
        if (visible) {
            if (questSidebar == null) {
                questSidebar = questCockpitInstance.get();
                RootPanel.get().add(questSidebar);
            }
        } else {
            if (questSidebar != null) {
                RootPanel.get().remove(questSidebar);
                questSidebar = null;
            }
        }
    }
}
