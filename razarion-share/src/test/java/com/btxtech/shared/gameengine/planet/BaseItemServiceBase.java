package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.MasterPlanetConfig;
import com.btxtech.shared.dto.InitialSlaveSyncItemInfo;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.LevelService;
import com.btxtech.shared.gameengine.StaticGameInitEvent;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncFactory;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncTurret;
import com.btxtech.shared.gameengine.planet.model.SyncWeapon;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import org.easymock.EasyMock;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 16.04.2017.
 */
@Deprecated
// WeldMasterBaseTest
public class BaseItemServiceBase {
    private BaseItemService baseItemService;
    private SyncItemContainerService syncItemContainerService;
    private ItemTypeService itemTypeService;

    protected void setup(PlanetConfig planetConfig, GameEngineMode gameEngineMode, MasterPlanetConfig masterPlanetConfig, InitialSlaveSyncItemInfo initialSlaveSyncItemInfo) {
        baseItemService = new BaseItemService();
        itemTypeService = new ItemTypeService();
        // SyncItemContainerService
        syncItemContainerService = new SyncItemContainerService();
        Map<Class, Supplier> selectorSupplier = new HashMap<>();
        selectorSupplier.put(SyncBaseItem.class, () -> {
            SyncBaseItem syncBaseItem = new SyncBaseItem();
            Map<Class, Supplier> instanceSupplier = new HashMap<>();
            instanceSupplier.put(SyncBuilder.class, () -> {
                SyncBuilder syncBuilder = new SyncBuilder();
                SimpleTestEnvironment.injectService("itemTypeService", syncBuilder, itemTypeService);
                return syncBuilder;
            });
            instanceSupplier.put(SyncFactory.class, () -> {
                SyncFactory syncFactory = new SyncFactory();
                SimpleTestEnvironment.injectService("itemTypeService", syncFactory, itemTypeService);
                return syncFactory;
            });
            instanceSupplier.put(SyncWeapon.class, () -> {
                SyncWeapon syncWeapon = new SyncWeapon();
                SimpleTestEnvironment.injectInstance("syncTurretInstance", syncWeapon, SyncTurret::new);
                SimpleTestEnvironment.injectService("syncItemContainerService", syncWeapon, getSyncItemContainerService());
                return syncWeapon;
            });
            SimpleTestEnvironment.injectInstance("instance", syncBaseItem, instanceSupplier);
            GameLogicService gameLogicServiceMock = EasyMock.createNiceMock(GameLogicService.class);
            EasyMock.replay(gameLogicServiceMock);
            SimpleTestEnvironment.injectService("gameLogicService", syncBaseItem, gameLogicServiceMock);
            return syncBaseItem;
        });
        SimpleTestEnvironment.injectInstance("syncItemInstance", syncItemContainerService, selectorSupplier);
        SimpleTestEnvironment.injectInstance("syncPhysicalMovableInstance", syncItemContainerService, () -> {
            SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
            SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, syncItemContainerService);
            TerrainService terrainServiceMock = EasyMock.createNiceMock(TerrainService.class);
            EasyMock.expect(terrainServiceMock.getSurfaceAccess().getInterpolatedZ(EasyMock.anyObject(DecimalPosition.class))).andReturn(-1.7);
            EasyMock.replay(terrainServiceMock);
            SimpleTestEnvironment.injectService("terrainService", syncPhysicalMovable, SyncPhysicalArea.class, terrainServiceMock);
            return syncPhysicalMovable;
        });
        SimpleTestEnvironment.injectService("syncItemContainerService", baseItemService, syncItemContainerService);
        // Level
        LevelService levelService = new LevelService();
        levelService.init(new StaticGameConfig().setLevelConfigs(GameTestContent.setupLevelConfigs()));
        SimpleTestEnvironment.injectService("levelService", baseItemService, levelService);

        GameLogicService gameLogicServiceMock = EasyMock.createNiceMock(GameLogicService.class);
        EasyMock.replay(gameLogicServiceMock);
        SimpleTestEnvironment.injectService("gameLogicService", baseItemService, gameLogicServiceMock);

        // Setup ItemTypeService
        setupItemTypeService(itemTypeService);
        SimpleTestEnvironment.injectService("itemTypeService", baseItemService, itemTypeService);

        planetConfig.setItemTypeLimitation(GameTestContent.setupPlanetItemTypeLimitations());

        baseItemService.onPlanetActivation(new PlanetActivationEvent(planetConfig, gameEngineMode, masterPlanetConfig, PlanetActivationEvent.Type.INITIALIZE));
    }

    private void setupItemTypeService(ItemTypeService itemTypeService) {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setBaseItemTypes(GameTestContent.setupBaseItemType());
        itemTypeService.onGameEngineInit(new StaticGameInitEvent(staticGameConfig));
    }

    public BaseItemService getBaseItemService() {
        return baseItemService;
    }

    public SyncItemContainerService getSyncItemContainerService() {
        return syncItemContainerService;
    }

    public BaseItemType getBaseItemType(int baseItemTypeId) {
        return itemTypeService.getBaseItemType(baseItemTypeId);
    }
}