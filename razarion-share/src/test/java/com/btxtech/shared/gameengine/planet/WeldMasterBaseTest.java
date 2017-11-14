package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.SlaveSyncItemInfo;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class WeldMasterBaseTest extends WeldBaseTest {
    private int nextHumanPlayerId = 1;

    protected void setupMasterEnvironment() {
        setupMasterEnvironment(GameTestContent.setupStaticGameConfig(), null);
    }

    protected void setupMasterEnvironment(StaticGameConfig staticGameConfig, List<TerrainSlopePosition> terrainSlopePositions) {
        setupEnvironment(staticGameConfig, GameTestContent.setupPlanetConfig());
        getTestNativeTerrainShapeAccess().setTerrainSlopePositions(terrainSlopePositions);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), null, () -> getPlanetService().start(), null);
    }

    protected CommandService getCommandService() {
        return getWeldBean(CommandService.class);
    }

    protected BotService getBotService() {
        return getWeldBean(BotService.class);
    }

    protected MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

    protected PlayerBaseFull createHumanBaseWithBaseItem(DecimalPosition position, UserContext userContext) {
        return getBaseItemService().createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getHumanPlayerId(), userContext.getName(), position);
    }

    protected PlayerBaseFull findBotBase(int botId) {
        BotService botService = getBotService();
        return botService.getBotRunner(botId).getBase();
    }

    public SlaveSyncItemInfo getSlaveSyncItemInfo(UserContext userContext) {
        return getPlanetService().generateSlaveSyncItemInfo(userContext);
    }

    protected UserContext createLevel1UserContext(Integer userId) {
        int humanPlayerId = nextHumanPlayerId++;
        return new UserContext().setLevelId(GameTestContent.LEVEL_ID_1).setUnlockedItemLimit(Collections.emptyMap()).setHumanPlayerId(new HumanPlayerId().setPlayerId(humanPlayerId).setUserId(userId)).setName("test base " + humanPlayerId);
    }

    protected UserContext createLevel1UserContext() {
        return createLevel1UserContext(null);
    }

    public SyncBaseItem fabricateAndMove(SyncBaseItem factory, int baseItemTypeId, DecimalPosition position, PlayerBaseFull playerBaseFull) {
        getCommandService().fabricate(factory, getBaseItemType(baseItemTypeId));
        tickPlanetServiceBaseServiceActive();
        SyncBaseItem syncBaseItem = findSyncBaseItemHighestId(playerBaseFull, baseItemTypeId);
        getCommandService().move(syncBaseItem, position);
        tickPlanetServiceBaseServiceActive();
        return syncBaseItem;
    }

}
