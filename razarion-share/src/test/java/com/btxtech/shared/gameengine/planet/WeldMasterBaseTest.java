package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;

import java.util.ArrayList;

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class WeldMasterBaseTest extends WeldBaseTest {
    private int nextHumanPlayerId;

    protected void setupMasterEnvironment() {
        nextHumanPlayerId = 1;
        setupEnvironment(GameTestContent.setupStaticGameConfig(), GameTestContent.setupPlanetConfig());
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), null, () -> getPlanetService().start(), null);
    }

    protected CommandService getCommandService() {
        return getWeldBean(CommandService.class);
    }

    protected MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

    protected PlayerBaseFull createHumanBaseWithBaseItem(DecimalPosition position, UserContext userContext) {
        return getBaseItemService().createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getHumanPlayerId(), userContext.getName(), position);
    }

    public SlaveSyncItemInfo getSlaveSyncItemInfo(UserContext userContext) {
        return getPlanetService().generateSlaveSyncItemInfo(userContext);
    }

    protected UserContext createLevel1UserContext() {
        int humanPlayerId = nextHumanPlayerId++;
        return new UserContext().setLevelId(GameTestContent.LEVEL_ID_1).setHumanPlayerId(new HumanPlayerId().setPlayerId(humanPlayerId)).setName("test base " + humanPlayerId);
    }


}
