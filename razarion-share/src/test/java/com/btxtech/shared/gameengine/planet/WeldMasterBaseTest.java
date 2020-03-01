package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.packets.PlayerBaseInfo;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

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
        setupMasterEnvironment(FallbackConfig.setupStaticGameConfig(), null);
    }

    protected void setupMasterEnvironment(StaticGameConfig staticGameConfig, List<TerrainSlopePosition> terrainSlopePositions) {
        setupEnvironment(staticGameConfig, FallbackConfig.setupPlanetConfig());
        getTestNativeTerrainShapeAccess().setTerrainSlopeAndObjectPositions(terrainSlopePositions, null);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), () -> getPlanetService().start(), null);
    }

    protected CommandService getCommandService() {
        return getWeldBean(CommandService.class);
    }

    protected BotService getBotService() {
        return getWeldBean(BotService.class);
    }

    protected PathingService getPathingService() {
        return getWeldBean(PathingService.class);
    }

    @Deprecated // Use getBotBase(int botId)
    protected PlayerBase getBotBase(String botName) {
        for (PlayerBaseInfo playerBaseInfo : getBaseItemService().getPlayerBaseInfos()) {
            if (playerBaseInfo.getCharacter().isBot()) {
                if (playerBaseInfo.getName().equalsIgnoreCase(botName)) {
                    return getBaseItemService().getPlayerBase4BaseId(playerBaseInfo.getBaseId());
                }
            }
        }
        throw new IllegalArgumentException("No botbase found for: " + botName);
    }

    protected PlayerBase getBotBase(int botId) {
        for (PlayerBaseInfo playerBaseInfo : getBaseItemService().getPlayerBaseInfos()) {
            if (playerBaseInfo.getCharacter().isBot() && playerBaseInfo.getBotId() == botId) {
                return getBaseItemService().getPlayerBase4BaseId(playerBaseInfo.getBaseId());
            }
        }
        throw new IllegalArgumentException("No bot base found botId: " + botId);
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

    public InitialSlaveSyncItemInfo getSlaveSyncItemInfo(UserContext userContext) {
        return getPlanetService().generateSlaveSyncItemInfo(userContext.getHumanPlayerId());
    }

    protected UserContext createLevel1UserContext(Integer userId) {
        int humanPlayerId = nextHumanPlayerId++;
        return new UserContext().setLevelId(FallbackConfig.LEVEL_ID_1).setUnlockedItemLimit(Collections.emptyMap()).setHumanPlayerId(new HumanPlayerId().setPlayerId(humanPlayerId).setUserId(userId)).setName("test base " + humanPlayerId);
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
