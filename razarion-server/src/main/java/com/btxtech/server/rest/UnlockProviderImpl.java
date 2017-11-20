package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerInventoryService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.shared.rest.UnlockProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class UnlockProviderImpl implements UnlockProvider {
    @Inject
    private LevelPersistence levelPersistence;
    @Inject
    private ServerUnlockService serverUnlockService;
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private SessionHolder sessionHolder;
    @Inject
    private ServerInventoryService serverInventoryService;

    @Override
    public UnlockResultInfo unlockViaCrystals(int levelUnlockConfigId) {
        try {
            int crystals = serverInventoryService.loadCrystals(sessionHolder.getPlayerSession());
            if (crystals < levelPersistence.readLevelUnlockEntityCrystals(levelUnlockConfigId)) {
                return new UnlockResultInfo().setNotEnoughCrystals(true);
            }
            serverUnlockService.unlockViaCrystals(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), levelUnlockConfigId);
            return new UnlockResultInfo().setAvailableUnlocks(serverUnlockService.gatherAvailableUnlocks(sessionHolder.getPlayerSession().getUserContext().getHumanPlayerId(), sessionHolder.getPlayerSession().getUserContext().getLevelId()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
