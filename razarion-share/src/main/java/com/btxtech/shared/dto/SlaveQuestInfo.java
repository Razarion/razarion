package com.btxtech.shared.dto;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.packets.QuestProgressInfo;

/**
 * Created by Beat
 * on 09.08.2017.
 */
public class SlaveQuestInfo {
    private QuestConfig activeQuest;
    private QuestProgressInfo questProgressInfo;

    public QuestConfig getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfig activeQuest) {
        this.activeQuest = activeQuest;
    }

    public QuestProgressInfo getQuestProgressInfo() {
        return questProgressInfo;
    }

    public void setQuestProgressInfo(QuestProgressInfo questProgressInfo) {
        this.questProgressInfo = questProgressInfo;
    }

}
