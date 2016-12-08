package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.uiservice.cockpit.QuestVisualizer;

/**
 * Created by Beat
 * 11.07.2016.
 */
public class DevToolsQuestVisualizerImpl implements QuestVisualizer {

    @Override
    public void showSideBar(QuestDescriptionConfig descriptionConfig) {
        System.out.println("++++ DevToolsQuestVisualizerImpl.showSideBar(): " + descriptionConfig);
    }

    @Override
    public void setShowInGameVisualisation(boolean showInGameVisualisation) {
        // TODO
    }
}
