package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

/**
 * Created by Beat
 * 26.10.2016.
 */
public interface QuestListener {
    void onQuestPassed(HumanPlayerId humanPlayerId, QuestConfig questConfig);
}
