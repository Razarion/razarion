package com.btxtech.server.rest.engine;

import com.btxtech.server.gameengine.ServerUnlockService;
import com.btxtech.server.rest.ui.GameUiContextControllerImpl;
import com.btxtech.server.service.engine.LevelCrudPersistence;
import com.btxtech.server.service.engine.ServerInventoryService;
import com.btxtech.server.web.SessionService;
import com.btxtech.shared.gameengine.datatypes.config.LevelUnlockConfig;
import com.btxtech.shared.gameengine.datatypes.packets.UnlockResultInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest/unlock-controller")
public class UnlockController {
    private final Logger logger = LoggerFactory.getLogger(UnlockController.class);
    private final LevelCrudPersistence levelCrudPersistence;
    private final ServerUnlockService serverUnlockService;
    private final SessionService sessionService;
    private final ServerInventoryService serverInventoryService;

    public UnlockController(LevelCrudPersistence levelCrudPersistence, ServerUnlockService serverUnlockService, SessionService sessionService, ServerInventoryService serverInventoryService) {
        this.levelCrudPersistence = levelCrudPersistence;
        this.serverUnlockService = serverUnlockService;
        this.sessionService = sessionService;
        this.serverInventoryService = serverInventoryService;
    }

    @PostMapping(value = "unlockViaCrystals/{levelUnlockConfigId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public UnlockResultInfo unlockViaCrystals(@PathVariable("levelUnlockConfigId") int levelUnlockConfigId) {
        try {
            var playerSession = sessionService.getSession(GameUiContextControllerImpl.getCurrentHttpSessionId());
            int crystals = serverInventoryService.loadCrystals(playerSession);
            if (crystals < levelCrudPersistence.readLevelUnlockEntityCrystals(levelUnlockConfigId)) {
                return new UnlockResultInfo().setNotEnoughCrystals(true);
            }
            serverUnlockService.unlockViaCrystals(playerSession.getUserContext().getUserId(), levelUnlockConfigId);
            return new UnlockResultInfo().setAvailableUnlocks(serverUnlockService.getAvailableLevelUnlockConfigs(playerSession.getUserContext(), playerSession.getUserContext().getLevelId()));
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }

    @GetMapping(value = "available-level-unlockConfigs", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<LevelUnlockConfig> getAvailableLevelUnlockConfigs() {
        try {
            var playerSession = sessionService.getSession(GameUiContextControllerImpl.getCurrentHttpSessionId());
            return serverUnlockService.getAvailableLevelUnlockConfigs(playerSession.getUserContext(), playerSession.getUserContext().getLevelId());
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
            throw t;
        }
    }
}
