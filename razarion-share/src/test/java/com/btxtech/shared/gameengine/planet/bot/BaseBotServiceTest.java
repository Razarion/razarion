package com.btxtech.shared.gameengine.planet.bot;

import com.btxtech.shared.cdimock.TestSimpleExecutorService;
import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BaseItemServiceBase;
import com.btxtech.shared.gameengine.planet.PlanetActivationEvent;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.system.SimpleExecutorService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.List;

/**
 * Created by Beat
 * 03.05.2017.
 */
public class BaseBotServiceTest {
    private BotService botService;
    private TestSimpleScheduledFuture botScheduledFuture;
    private TestSimpleExecutorService testSimpleExecutorService;
    private BaseItemService baseItemService;

    protected void setupEnvironment() {
        // Init weld
        Weld weld = new Weld();
        WeldContainer weldContainer = weld.initialize();
        botService = weldContainer.instance().select(BotService.class).get();
        ItemTypeService itemTypeService = weldContainer.instance().select(ItemTypeService.class).get();
        TerrainService terrainService = weldContainer.instance().select(TerrainService.class).get();
        TerrainTypeService terrainTypeService = weldContainer.instance().select(TerrainTypeService.class).get();
        testSimpleExecutorService = weldContainer.instance().select(TestSimpleExecutorService.class).get();
        baseItemService = weldContainer.instance().select(BaseItemService.class).get();
        // Setup game environment
        BaseItemServiceBase.setupItemTypeService(itemTypeService);
        PlanetConfig planetConfig = new PlanetConfig().setGroundMeshDimension(new Rectangle(0, 0, 1000, 1000)).setGameEngineMode(GameEngineMode.MASTER);
        terrainService.setup(planetConfig);
        terrainTypeService.init(new GameEngineConfig().setGroundSkeletonConfig(new GroundSkeletonConfig().setHeights(new double[][]{{0.0}}).setHeightXCount(1).setHeightYCount(1)));
        baseItemService.onPlanetActivation(new PlanetActivationEvent(planetConfig, PlanetActivationEvent.Type.INITIALIZE));
    }

    protected void startBot(List<BotConfig> botConfigs) {
        botService.startBots(botConfigs);
        botScheduledFuture = testSimpleExecutorService.getScheduleAtFixedRate(SimpleExecutorService.Type.BOT);
    }

    protected void tickBotRunner() {
        botScheduledFuture.invokeRun();
    }

    protected void tickBaseItemService() {
        baseItemService.tick();
    }

    protected List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return baseItemService.getSyncBaseItemInfos();
    }
}
