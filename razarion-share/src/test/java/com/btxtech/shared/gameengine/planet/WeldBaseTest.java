package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.cdimock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.cdimock.TestSimpleExecutorService;
import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.HumanPlayerId;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.system.SimpleExecutorService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Beat
 * on 21.08.2017.
 */
public class WeldBaseTest {
    public static final int GROUND_SKELETON_ID = 1;
    private WeldContainer weldContainer;
    private TestSimpleExecutorService testSimpleExecutorService;
    private BaseItemService baseItemService;
    private int nextHumanPlayerId;
    private TestGameLogicListener testGameLogicListener;

    protected void setupEnvironment() {
        nextHumanPlayerId = 1;
        PlanetConfig planetConfig = setupPlanetConfig();
        // Init weld
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        testSimpleExecutorService = getWeldBean(TestSimpleExecutorService.class);
        baseItemService = getWeldBean(BaseItemService.class);
        // Setup game environment
        weldContainer.event().select(StaticGameInitEvent.class).fire(new StaticGameInitEvent(setupStaticGameConfig()));
        getWeldBean(TestNativeTerrainShapeAccess.class).setPlanetConfig(planetConfig);
        getWeldBean(PlanetService.class).initialise(planetConfig, GameEngineMode.MASTER, setupMasterPlanetConfig(), null, () -> {
            getWeldBean(PlanetService.class).start(null);
        }, null);
        testGameLogicListener = new TestGameLogicListener();
        getWeldBean(GameLogicService.class).setGameLogicListener(testGameLogicListener);
        ////
//        BaseItemServiceBase.setupItemTypeService(getWeldBean(ItemTypeService.class));
//        StaticGameConfig staticGameConfig = );
//        getWeldBean(TerrainTypeService.class).init(staticGameConfig);
//        getWeldBean(LevelService.class).init(staticGameConfig);
//        getWeldBean(TerrainService.class).setup(planetConfig, () -> baseItemService.onPlanetActivation(new PlanetActivationEvent(planetConfig, GameEngineMode.MASTER, null, null, PlanetActivationEvent.Type.INITIALIZE)), null);
    }

    protected <T> T getWeldBean(Class<T> clazz) {
        return weldContainer.instance().select(clazz).get();
    }

    protected TestSimpleExecutorService getTestSimpleExecutorService() {
        return testSimpleExecutorService;
    }

    protected CommandService getCommandService() {
        return getWeldBean(CommandService.class);
    }

    protected void tickBaseItemService() {
        baseItemService.tick();
    }

    protected List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return baseItemService.getSyncBaseItemInfos();
    }

    protected PlayerBaseFull createHumanBaseWithBaseItem(DecimalPosition position) {
        return baseItemService.createHumanBaseWithBaseItem(BaseItemServiceBase.LEVEL_ID_1, new HumanPlayerId().setPlayerId(nextHumanPlayerId++), "test base", position);
    }

    protected void removeSyncItem(SyncBaseItem syncBaseItem) {
        baseItemService.removeSyncItem(syncBaseItem);
    }

    protected BaseItemType getBaseItemType(int baseItemTypeId) {
        return getWeldBean(ItemTypeService.class).getBaseItemType(baseItemTypeId);
    }

    private StaticGameConfig setupStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setGroundSkeletonConfig(new GroundSkeletonConfig().setId(GROUND_SKELETON_ID).setHeights(new double[][]{{0.0}}).setHeightXCount(1).setHeightYCount(1));
        staticGameConfig.setLevelConfigs(BaseItemServiceBase.setupLevelConfigs());
        staticGameConfig.setBaseItemTypes(BaseItemServiceBase.setupBaseItemType());
        return staticGameConfig;
    }

    private PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setItemTypeLimitation(BaseItemServiceBase.setupItemTypeLimitations());
        planetConfig.setTerrainTileDimension(new Rectangle(0, 0, 1000, 1000));
        planetConfig.setPlayGround(new Rectangle2D(0, 0, 6, 6));
        planetConfig.setStartBaseItemTypeId(BaseItemServiceBase.BUILDER_ITEM_TYPE_ID);
        return planetConfig;
    }

    private MasterPlanetConfig setupMasterPlanetConfig() {
        MasterPlanetConfig masterPlanetConfig = new MasterPlanetConfig();
        masterPlanetConfig.setResourceRegionConfigs(new ArrayList<>());
        return masterPlanetConfig;
    }

    protected boolean isBaseServiceActive() {
        Collection<SyncBaseItem> activeItems = (Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService);
        Collection<SyncBaseItem> activeItemQueue = (Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService);
        return !activeItems.isEmpty() || !activeItemQueue.isEmpty();
    }

    protected void tickBaseService() {
        long tickCount = 0;
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        while (isBaseServiceActive()) {
            gameEngine.invokeRun();
            tickCount++;
        }
        System.out.println("Tick count: " + tickCount);
    }

    protected void tickBaseService(long count) {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        for (long i = 0; i < count; i++) {
            gameEngine.invokeRun();
        }
    }

    protected SyncBaseItem findSyncBaseItem(PlayerBaseFull playerBaseFull, int baseItemTypeId, SyncBaseItem... exclusion) {
        List<SyncBaseItem> exclusionList = Arrays.asList(exclusion);
        List<SyncBaseItem> found = new ArrayList<>();

        for (SyncBaseItem syncBaseItem : playerBaseFull.getItems()) {
            if (syncBaseItem.getBaseItemType().getId() != baseItemTypeId) {
                continue;
            }
            if (exclusionList.contains(syncBaseItem)) {
                continue;
            }
            found.add(syncBaseItem);
        }

        if (found.isEmpty()) {
            throw new IllegalArgumentException("No SyncBaseItem found for id: " + baseItemTypeId);
        }
        if (found.size() > 1) {
            throw new IllegalArgumentException("More then one SyncBaseItem found for id: " + baseItemTypeId);
        }
        return found.get(0);
    }

    protected TestGameLogicListener getTestGameLogicListener() {
        return testGameLogicListener;
    }
}
