package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.cdimock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.cdimock.TestSimpleExecutorService;
import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.gameengine.InventoryTypeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.gui.WeldDisplay;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTile;
import com.btxtech.shared.system.SimpleExecutorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 23.08.2017.
 */
public class WeldBaseTest {
    private WeldContainer weldContainer;
    private TestSimpleExecutorService testSimpleExecutorService;
    private BaseItemService baseItemService;
    private TestGameLogicListener testGameLogicListener;
    private PlanetConfig planetConfig;
    private StaticGameConfig staticGameConfig;
    private PlanetService planetService;

    protected void setupEnvironment(StaticGameConfig staticGameConfig, PlanetConfig planetConfig) {
        this.staticGameConfig = staticGameConfig;
        this.planetConfig = planetConfig;
        // Init weld
        Weld weld = new Weld();
        weldContainer = weld.initialize();
        testSimpleExecutorService = getWeldBean(TestSimpleExecutorService.class);
        // Init static game
        baseItemService = getWeldBean(BaseItemService.class);
        planetService = getWeldBean(PlanetService.class);
        testGameLogicListener = new TestGameLogicListener();
        getWeldBean(GameLogicService.class).setGameLogicListener(testGameLogicListener);
        fireStaticGameConfig(staticGameConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
    }

    public <T> T getWeldBean(Class<T> clazz) {
        return weldContainer.instance().select(clazz).get();
    }

    public TestSimpleExecutorService getTestSimpleExecutorService() {
        return testSimpleExecutorService;
    }

    public BaseItemService getBaseItemService() {
        return baseItemService;
    }

    public SyncItemContainerService getSyncItemContainerService() {
        return getWeldBean(SyncItemContainerService.class);
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
        return getWeldBean(TerrainService.class);
    }

    public TestNativeTerrainShapeAccess getTestNativeTerrainShapeAccess() {
        return getWeldBean(TestNativeTerrainShapeAccess.class);
    }

    public List<SyncBaseItemInfo> getSyncBaseItemInfos() {
        return baseItemService.getSyncBaseItemInfos();
    }

    public void removeSyncItem(SyncBaseItem syncBaseItem) {
        baseItemService.removeSyncItem(syncBaseItem);
    }

    public void fireStaticGameConfig(StaticGameConfig staticGameConfig) {
        weldContainer.event().select(StaticGameInitEvent.class).fire(new StaticGameInitEvent(staticGameConfig));
    }

    public boolean isBaseServiceActive(SyncBaseItem... ignores) {
        Collection<SyncBaseItem> activeItems = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService));
        activeItems.removeAll(Arrays.asList(ignores));
        Collection<SyncBaseItem> activeItemQueue = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService));
        activeItemQueue.removeAll(Arrays.asList(ignores));
        return !activeItems.isEmpty() || !activeItemQueue.isEmpty();
    }

    public boolean isPathingServiceMoving() {
        return getSyncItemContainerService().iterateOverBaseItems(false, false, false, syncBaseItem -> {
            SyncPhysicalArea syncPhysicalArea = syncBaseItem.getSyncPhysicalArea();
            if (!syncPhysicalArea.canMove()) {
                return null;
            }

            SyncPhysicalMovable syncPhysicalMovable = (SyncPhysicalMovable) syncPhysicalArea;
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
        return getWeldBean(ItemTypeService.class).getBaseItemType(baseItemTypeId);
    }

    public PlanetConfig getPlanetConfig() {
        return planetConfig;
    }

    public StaticGameConfig getStaticGameConfig() {
        return staticGameConfig;
    }

    public PlanetService getPlanetService() {
        return planetService;
    }

    public PlayerBase getPlayerBase(UserContext userContext) {
        return baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
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

    public static void assertContainingSyncItemIds(Collection<? extends SyncItem> expected, int... actualIds) {
        List<Integer> expectedIds = expected.stream().map(SyncItem::getId).collect(Collectors.toList());
        for (Integer actualId : actualIds) {
            Assert.assertTrue("Item does not exist: " + actualId, expectedIds.remove(actualId));
        }
        Assert.assertTrue("There are remaining items: " + expectedIds, expectedIds.isEmpty());
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
        getWeldBean(WeldDisplay.class).show(userObject);
    }

    public void exportTriangles(String filename, Index... terrainTileIndices) {
        List<Double> positions = new ArrayList<>();
        List<Double> norms = new ArrayList<>();
        List<Double> uvs = new ArrayList<>();
        Map<String, List<Double>> slope = new HashMap<>();
        Arrays.stream(terrainTileIndices).forEach(terrainTileIndex -> {
            TerrainTile terrainTile = getTerrainService().generateTerrainTile(terrainTileIndex);
            if (terrainTile.getTerrainSlopeTiles() != null) {
                Arrays.stream(terrainTile.getTerrainSlopeTiles()).forEach(terrainSlopeTile -> {
                    Arrays.stream(terrainSlopeTile.getVertices()).forEach(positions::add);
                    Arrays.stream(terrainSlopeTile.getNorms()).forEach(norms::add);
                    Arrays.stream(terrainSlopeTile.getUvs()).forEach(uvs::add);
                });
            }
        });
        slope.put("positions", positions);
        slope.put("norms", norms);
        slope.put("uvs", uvs);
        try {
            new ObjectMapper().writeValue(new File(filename), slope);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SlopeNode[][] toColumnRow(SlopeNode[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        SlopeNode[][] columnRow = new SlopeNode[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

    protected double[][] toColumnRow(double[][] rowColumn) {
        int xCount = rowColumn[0].length;
        int yCount = rowColumn.length;
        double[][] columnRow = new double[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                columnRow[x][y] = rowColumn[y][x];
            }
        }
        return columnRow;
    }

}
