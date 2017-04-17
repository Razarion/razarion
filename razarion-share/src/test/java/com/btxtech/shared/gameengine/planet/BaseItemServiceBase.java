package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.SimpleTestEnvironment;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.GameEngineInitEvent;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncBuilder;
import com.btxtech.shared.gameengine.planet.model.SyncFactory;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalArea;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.model.SyncTurret;
import com.btxtech.shared.gameengine.planet.model.SyncWeapon;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import org.easymock.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by Beat
 * 16.04.2017.
 */
public class BaseItemServiceBase {
    protected static final int BUILDER_ITEM_TYPE_ID = 1;
    protected static final int FACTORY_ITEM_TYPE_ID = 2;
    protected static final int ATTACKER_ITEM_TYPE_ID = 3;
    private BaseItemService baseItemService;
    private SyncItemContainerService syncItemContainerService;
    private ItemTypeService itemTypeService;

    protected void setup(PlanetConfig planetConfig) {
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
            return syncBaseItem;
        });
        SimpleTestEnvironment.injectInstance("syncItemInstance", syncItemContainerService, selectorSupplier);
        SimpleTestEnvironment.injectInstance("syncPhysicalMovableInstance", syncItemContainerService, () -> {
            SyncPhysicalMovable syncPhysicalMovable = new SyncPhysicalMovable();
            SimpleTestEnvironment.injectService("syncItemContainerService", syncPhysicalMovable, syncItemContainerService);
            TerrainService terrainServiceMock = EasyMock.createNiceMock(TerrainService.class);
            EasyMock.expect(terrainServiceMock.getInterpolatedZ(EasyMock.anyObject(DecimalPosition.class))).andReturn(-1.7);
            EasyMock.replay(terrainServiceMock);
            SimpleTestEnvironment.injectService("terrainService", syncPhysicalMovable, SyncPhysicalArea.class, terrainServiceMock);
            return syncPhysicalMovable;
        });

        SimpleTestEnvironment.injectService("syncItemContainerService", baseItemService, syncItemContainerService);

        // Setup ItemTypeService
        List<BaseItemType> baseItemTypes = new ArrayList<>();
        setupBuilder(baseItemTypes);
        setupFactory(baseItemTypes);
        setupAttacker(baseItemTypes);
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setBaseItemTypes(baseItemTypes);
        itemTypeService.onGameEngineInit(new GameEngineInitEvent(gameEngineConfig));
        SimpleTestEnvironment.injectService("itemTypeService", baseItemService, itemTypeService);

        baseItemService.onPlanetActivation(new PlanetActivationEvent(planetConfig));
    }

    protected void setupBuilder(List<BaseItemType> baseItemTypes) {
        BaseItemType bulldozer = new BaseItemType();
        bulldozer.setId(BUILDER_ITEM_TYPE_ID);
        bulldozer.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(2).setSpeed(20.0));
        bulldozer.setBuilderType(new BuilderType().setAbleToBuildIds(Collections.singletonList(FACTORY_ITEM_TYPE_ID)).setAnimationOrigin(new Vertex(3, 5, 17)).setProgress(5.1).setRange(2.7));
        baseItemTypes.add(bulldozer);
    }

    protected void setupFactory(List<BaseItemType> baseItemTypes) {
        BaseItemType factory = new BaseItemType();
        factory.setId(FACTORY_ITEM_TYPE_ID);
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(5).setSpeed(20.0));
        factory.setFactoryType(new FactoryType().setAbleToBuildIds(Arrays.asList(BUILDER_ITEM_TYPE_ID, ATTACKER_ITEM_TYPE_ID)).setProgress(2.9));
        baseItemTypes.add(factory);
    }

    protected void setupAttacker(List<BaseItemType> baseItemTypes) {
        BaseItemType attacker = new BaseItemType();
        attacker.setId(ATTACKER_ITEM_TYPE_ID);
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(1.0).setAngularVelocity(Math.toRadians(30)).setRadius(3).setSpeed(20.0));
        attacker.setWeaponType(new WeaponType().setDamage(5).setRange(10).setProjectileSpeed(20.0).setReloadTime(2).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(40)).setMuzzlePosition(new Vertex(2, 0, 1)).setTorrentCenter(new Vertex(0, 0, 1))));
        baseItemTypes.add(attacker);
    }

    public BaseItemService getBaseItemService() {
        return baseItemService;
    }

    public SyncItemContainerService getSyncItemContainerService() {
        return syncItemContainerService;
    }
}