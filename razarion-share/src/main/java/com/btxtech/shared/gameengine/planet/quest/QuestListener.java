package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

/**
 * Created by Beat
 * 26.10.2016.
 */
public interface QuestListener {
    void onQuestPassed(String userId, QuestConfig questConfig);
}
