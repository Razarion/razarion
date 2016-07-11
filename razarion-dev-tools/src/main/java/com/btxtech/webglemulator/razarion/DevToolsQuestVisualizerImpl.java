package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.cockpit.QuestVisualizer;

/**
 * Created by Beat
 * 11.07.2016.
 */
public class DevToolsQuestVisualizerImpl implements QuestVisualizer {
    @Override
    public void showSideBar(boolean visible) {
        System.out.println("++++ DevToolsQuestVisualizerImpl.showSideBar(): " + visible);
    }
}
