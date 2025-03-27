package com.btxtech.server.service;

import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.ColdGameUiContext;
import com.btxtech.shared.dto.GameUiControlInput;
import com.btxtech.shared.dto.WarmGameUiContext;
import org.springframework.stereotype.Service;

@Service
public class GameUiContextService {

    public ColdGameUiContext loadCold(GameUiControlInput gameUiControlInput, UserContext userContext) {
        ColdGameUiContext coldGameUiContext = new ColdGameUiContext();
        // TODO coldGameUiContext.staticGameConfig(staticGameConfigPersistence.loadStaticGameConfig());
        coldGameUiContext.userContext(userContext);
        if (userContext.getLevelId() == null) {
            // TODO alarmService.riseAlarm(Alarm.Type.USER_HAS_NO_LEVEL, userContext.getUserId());
            // TODO userContext.levelId(levelCrudPersistence.getStarterLevelId());
        }
        // TODO coldGameUiContext.audioConfig(setupAudioConfig());
        // TODO coldGameUiContext.gameTipVisualConfig(setupGameTipVisualConfig());
        // TODO coldGameUiContext.inGameQuestVisualConfig(setupInGameQuestVisualConfig());
        if (gameUiControlInput.checkPlayback()) {
            // TODO coldGameUiContext.warmGameUiContext(trackerPersistence.setupWarmGameUiControlConfig(gameUiControlInput));
        } else {
            coldGameUiContext.warmGameUiContext(loadWarm(userContext));
        }
        return coldGameUiContext;
    }

    public WarmGameUiContext loadWarm(UserContext userContext) {
        if (userContext.getLevelId() == null) {
            return null;
        }
        WarmGameUiContext warmGameUiContext = new WarmGameUiContext();
//  TODO      GameUiContextEntity gameUiContextEntity = load4Level(userContext.getLevelId());
//        if (gameUiContextEntity == null) {
//            return null;
//        }
//        WarmGameUiContext warmGameUiContext = gameUiContextEntity.toGameWarmGameUiControlConfig();
//        if (warmGameUiContext.getGameEngineMode() == GameEngineMode.SLAVE) {
//            warmGameUiContext.setSlavePlanetConfig(serverGameEngineCrudPersistence.readSlavePlanetConfig(userContext.getLevelId()));
//            warmGameUiContext.setSlaveQuestInfo(serverLevelQuestService.getSlaveQuestInfo(userContext.getUserId()));
//            warmGameUiContext.setAvailableUnlocks(serverUnlockService.hasAvailableUnlocks(userContext));
//        }
        return warmGameUiContext;
    }
}
