package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.dto.MasterPlanetConfig;
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

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class DaggerMasterBaseTest extends AbstractDaggerIntegrationTest {

    protected void setupMasterEnvironment() {
        setupMasterEnvironment(FallbackConfig.setupStaticGameConfig());
    }

    protected void setupMasterEnvironment(StaticGameConfig staticGameConfig) {
        setupEnvironment(staticGameConfig, FallbackConfig.setupPlanetConfig());
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), () -> getPlanetService().start(), null);
    }

    protected CommandService getCommandService() {
        return getTestShareDagger().commandService();
    }

    protected BotService getBotService() {
        return getWeldBean(BotService.class);
    }

    protected PathingService getPathingService() {
        return getTestShareDagger().pathingService();
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
        return getBaseItemService().createHumanBaseWithBaseItem(userContext.getLevelId(), userContext.getUnlockedItemLimit(), userContext.getUserId(), userContext.getName(), position);
    }

    protected PlayerBaseFull findBotBase(int botId) {
        BotService botService = getBotService();
        return botService.getBotRunner(botId).getBase();
    }

    public InitialSlaveSyncItemInfo getSlaveSyncItemInfo(UserContext userContext) {
        return getPlanetService().generateSlaveSyncItemInfo(userContext.getUserId());
    }

    protected UserContext createLevel1UserContext(String userId) {
        return new UserContext().levelId(FallbackConfig.LEVEL_ID_1).unlockedItemLimit(Collections.emptyMap()).userId(userId).name("test user id:" + userId);
    }

    protected UserContext createLevel1UserContext() {
        return createLevel1UserContext("00001");
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
