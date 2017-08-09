package com.btxtech.shared.gameengine.planet.quest;


import com.btxtech.shared.gameengine.planet.GameLogicService;

import javax.inject.Inject;

/**
 * User: beat
 * Date: 07.09.13
 * Time: 11:31
 */
public abstract class AbstractUpdatingComparison implements AbstractComparison {
    private static final int MIN_SEND_DELAY = 1000;
    @Inject
    private GameLogicService gameLogicService;
    private long lastProgressSendTime;
    private boolean hasUpdateToSend;

    protected void onProgressChanged() {
        if (lastProgressSendTime + MIN_SEND_DELAY > System.currentTimeMillis()) {
            hasUpdateToSend = true;
            return;
        }
        gameLogicService.onQuestProgressUpdate(getAbstractConditionProgress().getHumanPlayerId(), generateQuestProgressInfo());
        lastProgressSendTime = System.currentTimeMillis();
        hasUpdateToSend = false;
    }

    @Override
    public void handleDeferredUpdate() {
        if (hasUpdateToSend) {
            onProgressChanged();
        }
    }

}
