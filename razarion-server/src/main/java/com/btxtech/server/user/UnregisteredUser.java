package com.btxtech.server.user;

import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;

import java.util.ArrayList;
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

    public void addCompletedQuestId(int questId) {
        if(completedQuestIds == null) {
            completedQuestIds = new ArrayList<>();
        }
        completedQuestIds.add(questId);
    }

    public void removeCompletedQuestId(int questId) {
        if(completedQuestIds == null) {
            return;
        }
        completedQuestIds.remove(questId);
    }

    public QuestConfig getActiveQuest() {
        return activeQuest;
    }

    public void setActiveQuest(QuestConfig activeQuest) {
        this.activeQuest = activeQuest;
    }
}
