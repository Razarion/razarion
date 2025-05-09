package com.btxtech.shared.gameengine.planet.quest;


import com.btxtech.shared.gameengine.planet.GameLogicService;

/**
 * User: beat
 * Date: 07.09.13
 * Time: 11:31
 */
public abstract class AbstractUpdatingComparison implements AbstractComparison {
    static int MIN_SEND_DELAY = 1000;
    // Only updated in quests. This is no proper solution. Better e.g. bot action delay. But too expensive...
    private final GameLogicService gameLogicService;
    private long lastProgressSendTime;
    private boolean hasUpdateToSend;
    private boolean minSendDelayEnabled;

    public AbstractUpdatingComparison(GameLogicService gameLogicService) {
        this.gameLogicService = gameLogicService;
    }

    protected void onProgressChanged() {
        if (minSendDelayEnabled) {
            if (lastProgressSendTime + MIN_SEND_DELAY > System.currentTimeMillis()) {
                hasUpdateToSend = true;
                return;
            }
        }
        if (getAbstractConditionProgress() != null) {
            // Causes problem in devtool, works in web mode -> magical
            gameLogicService.onQuestProgressUpdate(getAbstractConditionProgress().getUserId(), generateQuestProgressInfo());
        }
        lastProgressSendTime = System.currentTimeMillis();
        hasUpdateToSend = false;
    }

    @Override
    public void handleDeferredUpdate() {
        if (hasUpdateToSend) {
            onProgressChanged();
        }
    }

    public void setMinSendDelayEnabled(boolean minSendDelayEnabled) {
        this.minSendDelayEnabled = minSendDelayEnabled;
    }
}
