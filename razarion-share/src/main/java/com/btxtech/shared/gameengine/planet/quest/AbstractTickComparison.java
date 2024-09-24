package com.btxtech.shared.gameengine.planet.quest;

import com.btxtech.shared.gameengine.planet.GameLogicService;

/**
 * Created by Beat
 * on 06.03.2018.
 */
public abstract class AbstractTickComparison extends AbstractUpdatingComparison {

    public AbstractTickComparison(GameLogicService gameLogicService) {
        super(gameLogicService);
    }

    public abstract void tick();

}
