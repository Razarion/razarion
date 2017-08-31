package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.datatypes.config.QuestDescriptionConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;
import com.btxtech.uiservice.cockpit.QuestVisualizer;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestQuestVisualizer implements QuestVisualizer {
    @Override
    public void showSideBar(QuestDescriptionConfig descriptionConfig, QuestProgressInfo questProgressInfo, boolean showQuestSelectionButton) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShowInGameVisualisation(boolean showInGameVisualisation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onQuestProgress(QuestProgressInfo questProgressInfo) {
        throw new UnsupportedOperationException();
    }
}
