package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.cdimock.TestNativeTerrainShapeAccess;
import com.btxtech.shared.cdimock.TestSimpleExecutorService;
import com.btxtech.shared.cdimock.TestSimpleScheduledFuture;
import com.btxtech.shared.datatypes.SingleHolder;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.packets.SyncBaseItemInfo;
import com.btxtech.shared.gameengine.planet.energy.EnergyService;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBoxItem;
import com.btxtech.shared.gameengine.planet.model.SyncResourceItem;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.system.SimpleExecutorService;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        getWeldBean(TestNativeTerrainShapeAccess.class).setPlanetConfig(planetConfig);
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

    public EnergyService getEnergyService() {
        return getWeldBean(EnergyService.class);
    }

    public ResourceService getResourceService() {
        return getWeldBean(ResourceService.class);
    }

    public QuestService getQuestService() {
        return getWeldBean(QuestService.class);
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

    public boolean isBaseServiceActive(SyncBaseItem[] ignores) {
        Collection<SyncBaseItem> activeItems = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItems", baseItemService));
        activeItems.removeAll(Arrays.asList(ignores));
        Collection<SyncBaseItem> activeItemQueue = new ArrayList<>((Collection<SyncBaseItem>) SimpleTestEnvironment.readField("activeItemQueue", baseItemService));
        activeItemQueue.removeAll(Arrays.asList(ignores));
        return !activeItems.isEmpty() || !activeItemQueue.isEmpty();
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

    public void tickPlanetService(long count) {
        TestSimpleScheduledFuture gameEngine = getTestSimpleExecutorService().getScheduleAtFixedRate(SimpleExecutorService.Type.GAME_ENGINE);
        for (long i = 0; i < count; i++) {
            gameEngine.invokeRun();
        }
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

    protected PlanetService getPlanetService() {
        return planetService;
    }

    public PlayerBase getPlayerBase(UserContext userContext) {
        return baseItemService.getPlayerBase4HumanPlayerId(userContext.getHumanPlayerId());
    }

    public void assertSyncItemCount(int baseCount, int resourceCount, int boxCount) {
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
        Assert.assertEquals("Base items", baseCount, (int)actualBaseCount.getO());
        Assert.assertEquals("Resource items", resourceCount, (int)actualResourceCount.getO());
        Assert.assertEquals("Box items", boxCount, (int)actualBoxCount.getO());
    }

    public void printAllSyncItems() {
        System.out.println("---printAllSyncItems-------------------------------------------");
        getSyncItemContainerService().iterateOverItems(true, true, null, syncItem -> {
            System.out.println(syncItem);
            return null;
        });
        System.out.println("---------------------------------------------------------------");
    }
}