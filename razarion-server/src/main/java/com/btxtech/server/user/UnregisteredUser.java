package com.btxtech.server.user;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import java.util.Collection;

/**
 * Created by Beat
 * on 02.08.2017.
 */
public class UnregisteredUser {
    private Collection<Integer> completedQuestIds;
    private QuestConfig activeQuest;

    public Collection<Integer> getCompletedQuestIds() {
        return completedQuestIds;
    }

    public QuestConfig getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfig activeQuest) {
        this.activeQuest = activeQuest;
    }
}
