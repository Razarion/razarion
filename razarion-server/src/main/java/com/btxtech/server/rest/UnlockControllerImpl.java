package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerInventoryService;
import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.persistence.level.LevelCrudPersistence;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import com.btxtech.shared.rest.UnlockController;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Beat
 * on 23.09.2017.
 */
public class UnlockControllerImpl implements UnlockController {
    @Inject
    private LevelCrudPersistence levelCrudPersistence;
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
            if (crystals < levelCrudPersistence.readLevelUnlockEntityCrystals(levelUnlockConfigId)) {
                return new UnlockResultInfo().setNotEnoughCrystals(true);
            }
            serverUnlockService.unlockViaCrystals(sessionHolder.getPlayerSession().getUserContext().getUserId(), levelUnlockConfigId);
            return new UnlockResultInfo().setAvailableUnlocks(serverUnlockService.getAvailableLevelUnlockConfigs(sessionHolder.getPlayerSession().getUserContext(), sessionHolder.getPlayerSession().getUserContext().getLevelId()));
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }

    @Override
    public List<LevelUnlockConfig> getAvailableLevelUnlockConfigs() {
        try {
            return serverUnlockService.getAvailableLevelUnlockConfigs(sessionHolder.getPlayerSession().getUserContext(), sessionHolder.getPlayerSession().getUserContext().getLevelId());
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
            throw t;
        }
    }
}
