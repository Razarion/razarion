package com.btxtech.shared.gameengine.datatypes.config.bot;

/**
 * Created by Beat
 * on 28.03.2018.
 */
public class BotSceneIndicationInfo {
    private int botSceneId;
    private int conflictStep;
    private int conflictStepCount;

    public int getBotSceneId() {
        return botSceneId;
    }

    public BotSceneIndicationInfo setBotSceneId(int botSceneId) {
        this.botSceneId = botSceneId;
        return this;
    }

    public int getConflictStep() {
        return conflictStep;
    }

    public BotSceneIndicationInfo setConflictStep(int conflictStep) {
        this.conflictStep = conflictStep;
        return this;
    }

    public int getConflictStepCount() {
        return conflictStepCount;
    }

    public BotSceneIndicationInfo setConflictStepCount(int conflictStepCount) {
        this.conflictStepCount = conflictStepCount;
        return this;
    }
}
