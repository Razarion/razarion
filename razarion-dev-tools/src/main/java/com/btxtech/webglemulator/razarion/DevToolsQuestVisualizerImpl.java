package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.uiservice.cockpit.QuestVisualizer;

/**
 * Created by Beat
 * 11.07.2016.
 */
public class DevToolsQuestVisualizerImpl implements QuestVisualizer {

    @Override
    public void showSideBar(QuestConfig questConfig) {
        System.out.println("++++ DevToolsQuestVisualizerImpl.showSideBar(): " + questConfig);
    }
}
