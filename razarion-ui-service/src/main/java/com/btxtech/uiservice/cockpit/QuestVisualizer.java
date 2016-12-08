package com.btxtech.uiservice.cockpit;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;

/**
 * Created by Beat
 * 11.07.2016.
 */
public interface QuestVisualizer {
    void showSideBar(QuestDescriptionConfig descriptionConfig);

    void setShowInGameVisualisation(boolean showInGameVisualisation);
}
