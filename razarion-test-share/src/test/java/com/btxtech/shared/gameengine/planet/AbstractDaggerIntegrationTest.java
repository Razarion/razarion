package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.DaggerTestShareDagger;
import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.TestShareDagger;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.command.BaseCommand;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.datatypes.packets.TickInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.gui.WeldDisplay;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.mock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.mock.TestSimpleExecutorService;
import com.btxtech.shared.mock.TestSimpleScheduledFuture;
import com.btxtech.shared.mocks.TestFloat32Array;
import com.btxtech.shared.mocks.TestFloat32ArraySerializer;
import com.btxtech.shared.system.SimpleExecutorService;
import com.btxtech.shared.system.alarm.AlarmService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class AbstractDaggerIntegrationTest {
    private TestShareDagger testShareDagger;
    private BaseItemService baseItemService;
    private TestGameLogicListener testGameLogicListener;
    private PlanetConfig planetConfig;
    private StaticGameConfig staticGameConfig;

    public static void assertContainingSyncItemIds(Collection<? extends SyncItem> expected, int... actualIds) {
        List<Integer> expectedIds = expected.stream().map(SyncItem::getId).collect(Collectors.toList());
        for (Integer actualId : actualIds) {
            Assert.assertTrue("Item does not exist: " + actualId, expectedIds.remove(actualId));
        }
        Assert.assertTrue("There are remaining items: " + expectedIds, expectedIds.isEmpty());
    }

    protected void setupEnvironment(StaticGameConfig staticGameConfig, PlanetConfig planetConfig) {
        this.staticGameConfig = staticGameConfig;
        this.planetConfig = planetConfig;
        // Init Dagger
        testShareDagger = DaggerTestShareDagger.builder().build();
        // Init static game
        baseItemService = testShareDagger.baseItemService();
        testGameLogicListener = new TestGameLogicListener();
        testShareDagger.gameLogicService().setGameLogicListener(testGameLogicListener);
        fireStaticGameConfig(staticGameConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getTestNativeTerrainShapeAccess().loadHeightMap("/CompressedHeightMap.bin", AbstractDaggerIntegrationTest.class);
    }

    public TestShareDagger getTestShareDagger() {
        return testShareDagger;
    }

    @Deprecated
    public <T> T getWeldBean(Class<T> clazz) {
        throw new UnsupportedOperationException("... Use Dagger ...");
    }

    public TestSimpleExecutorService getTestSimpleExecutorService() {
        return testShareDagger.testSimpleExecutorService();
    }

    public BaseItemService getBaseItemService() {
        return baseItemService;
    }

    public SyncItemContainerServiceImpl getSyncItemContainerService() {
        return testShareDagger.syncItemContainerService();
    }

    public ItemTypeService getItemTypeService() {
        return getWeldBean(ItemTypeService.class);
    }

    public EnergyService getEnergyService() {
        return getWeldBean(EnergyService.class);
    }

    public ResourceService getResourceService() {
        return getWeldBean(ResourceService.class);
    }

    public QuestService getQuestService() {
        return getWeldBean(QuestService.class);
    }

    public BoxService getBoxService() {
        return getWeldBean(BoxService.class);
    }

    public InventoryTypeService getInventoryTypeService() {
        return getWeldBean(InventoryTypeService.class);
    }

    public TerrainService getTerrainService() {
        return testShareDagger.terrainService();
    }

    public TestNativeTerrainShapeAccess getTestNativeTerrainShapeAccess() {
        return testShareDagger.testNativeTerrainShapeAccess();
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return baseItemService.getSyncBaseItemInfos();
    }

    public void removeSyncItem(SyncBaseItem syncBaseItem) {
        baseItemService.removeSyncItem(syncBaseItem);
    }

    public void fireStaticGameConfig(StaticGameConfig staticGameConfig) {
        testShareDagger.initializeService().setStaticGameConfig(staticGameConfig);
    }

    public boolean isBaseServiceActive(SyncBaseItem... ignores) {
        Collection<SyncBaseItem> activeItems = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService));
        activeItems.removeAll(Arrays.asList(ignores));
        Collection<SyncBaseItem> activeItemQueue = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService));
        activeItemQueue.removeAll(Arrays.asList(ignores));
        Collection<BaseCommand> commandQueue = new ArrayList<>((Queue<BaseCommand>) SimpleTestEnvironment.readField("commandQueue", baseItemService));
        commandQueue.removeIf(baseCommand -> Arrays.stream(ignores).anyMatch(ignoredSyncBaseItem -> ignoredSyncBaseItem.getId() == baseCommand.getId()));
        Collection<TickInfo> pendingReceivedTickInfos = new ArrayList<>((PriorityQueue<TickInfo>) SimpleTestEnvironment.readField("pendingReceivedTickInfos", baseItemService));
        List<SyncBaseItemInfo> pendingReceivedSyncBaseItemInfos = pendingReceivedTickInfos.stream()
                .flatMap(tickInfo -> tickInfo.getSyncBaseItemInfos().stream())
                .filter(syncBaseItemInfo -> Arrays.stream(ignores).noneMatch(ignoredSyncBaseItem -> ignoredSyncBaseItem.getId() == syncBaseItemInfo.getId()))
                .collect(Collectors.toList());
        return !activeItems.isEmpty() || !activeItemQueue.isEmpty() || !commandQueue.isEmpty() || !pendingReceivedSyncBaseItemInfos.isEmpty();
    }

    public boolean isPathingServiceMoving() {
        return getSyncItemContainerService().iterateOverBaseItems(false, false, false, syncBaseItem -> {
            AbstractSyncPhysical abstractSyncPhysical = syncBaseItem.getAbstractSyncPhysical();
            if (!abstractSyncPhysical.canMove()) {
                return null;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) abstractSyncPhysical;
            if (syncPhysicalMovable.hasDestination() || syncPhysicalMovable.isMoving()) {
                return true;
            }
            return null;
        });
    }

    public void tickPlanetService() {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        gameEngine.invokeRun();
    }

    public void tickPlanetServiceBaseServiceActive(SyncBaseItem... ignores) {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        while (isBaseServiceActive(ignores)) {
            gameEngine.invokeRun();
        }
    }

    public void tickPlanetServicePathingActive() {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        while (isPathingServiceMoving()) {
            gameEngine.invokeRun();
        }
    }

    public void tickPlanetService(long count) {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        for (long i = 0; i < count; i++) {
            gameEngine.invokeRun();
        }
    }

    public void tickPlanetServiceSeconds(int seconds) {
        tickPlanetService(PlanetService.TICKS_PER_SECONDS * seconds);
    }

    public SyncBaseItem findSyncBaseItem(PlayerBaseFull playerBaseFull, int baseItemTypeId, SyncBaseItem... exclusion) {
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

    public SyncBaseItem findSyncBaseItemHighestId(PlayerBaseFull playerBaseFull, int baseItemTypeId) {
        SyncBaseItem found = null;

        for (SyncBaseItem syncBaseItem : playerBaseFull.getItems()) {
            if (syncBaseItem.getBaseItemType().getId() != baseItemTypeId) {
                continue;
            }
            if (found == null) {
                found = syncBaseItem;
            } else if (syncBaseItem.getId() > found.getId()) {
                found = syncBaseItem;
            }
        }

        if (found == null) {
            throw new IllegalArgumentException("No SyncBaseItem found for id: " + baseItemTypeId);
        }
        return found;
    }

    public SyncBoxItem findSyncBoxItem(int boxItemTypeId, SyncBoxItem... exclusion) {
        List<SyncBoxItem> exclusionList = Arrays.asList(exclusion);
        List<SyncBoxItem> found = new ArrayList<>();

        getSyncItemContainerService().iterateOverItems(false, true, null, syncItem -> {
            if (!(syncItem instanceof SyncBoxItem)) {
                return null;
            }
            SyncBoxItem syncBoxItem = (SyncBoxItem) syncItem;
            if (exclusionList.contains(syncBoxItem)) {
                return null;
            }
            found.add(syncBoxItem);
            return null;
        });

        if (found.isEmpty()) {
            throw new IllegalArgumentException("No SyncBoxItem found for id: " + boxItemTypeId);
        }
        if (found.size() > 1) {
            throw new IllegalArgumentException("More then one SyncBoxItem found for id: " + boxItemTypeId);
        }
        return found.get(0);
    }

    public TestGameLogicListener getTestGameLogicListener() {
        return testGameLogicListener;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) {
        return getTestShareDagger().itemTypeService().getBaseItemType(baseItemTypeId);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }

    public PlanetService getPlanetService() {
        return testShareDagger.planetService();
    }

    public AlarmService getAlarmService() {
        return testShareDagger.alarmService();
    }

    public PlayerBase getPlayerBase(UserContext userContext) {
        return baseItemService.getPlayerBase4UserId(userContext.getUserId());
    }

    public void assertSyncItemCount(int baseItemCount, int resourceCount, int boxCount) {
        SingleHolder<Integer> actualBaseCount = new SingleHolder<>(0);
        SingleHolder<Integer> actualResourceCount = new SingleHolder<>(0);
        SingleHolder<Integer> actualBoxCount = new SingleHolder<>(0);
        getSyncItemContainerService().iterateOverItems(true, true, null, syncItem -> {
            if (syncItem instanceof SyncBaseItem) {
                actualBaseCount.setO(actualBaseCount.getO() + 1);
            } else if (syncItem instanceof SyncResourceItem) {
                actualResourceCount.setO(actualResourceCount.getO() + 1);
            } else if (syncItem instanceof SyncBoxItem) {
                actualBoxCount.setO(actualBoxCount.getO() + 1);
            } else {
                throw new IllegalStateException("Unknwon item type: " + syncItem);
            }
            return null;
        });
        Assert.assertEquals("Base items", baseItemCount, (int) actualBaseCount.getO());
        Assert.assertEquals("Resource items", resourceCount, (int) actualResourceCount.getO());
        Assert.assertEquals("Box items", boxCount, (int) actualBoxCount.getO());
    }

    public void assertContainingSyncItems(List<SyncBaseItem> expected, SyncBaseItem... actuals) {
        List<SyncBaseItem> expectedCopy = new ArrayList<>(expected);
        for (SyncBaseItem actual : actuals) {
            Assert.assertTrue("Item does not exist: " + actual, expectedCopy.remove(actual));
        }
        Assert.assertTrue("There are remianing items: " + expectedCopy.size(), expectedCopy.isEmpty());
    }

    public void printAllSyncItems() {
        System.out.println("---printAllSyncItems-------------------------------------------");
        getSyncItemContainerService().iterateOverItems(true, true, null, syncItem -> {
            System.out.println(syncItem);
            return null;
        });
        System.out.println("---------------------------------------------------------------");
    }

    public void showDisplay(Object... userObject) {
        WeldDisplay.show(userObject, testShareDagger);
    }

    public void exportTriangles(String director, Index... terrainTileIndices) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(TestFloat32Array.class, new TestFloat32ArraySerializer());
            objectMapper.registerModule(module);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            // Export TerrainTile
            List<TerrainTile> terrainTiles = Arrays.stream(terrainTileIndices)
                    .map(terrainTileIndex -> getTerrainService().generateTerrainTile(terrainTileIndex))
                    .collect(Collectors.toList());
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(director, "terrain-tiles.json"), terrainTiles);

            // Export StaticGameConfig
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(director, "static-game-config.json"), getStaticGameConfig());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("exportTriangles(): " + new File(director));
    }

}
