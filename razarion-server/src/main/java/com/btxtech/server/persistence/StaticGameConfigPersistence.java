package com.btxtech.server.persistence;

import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.server.persistence.level.LevelPersistence;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ResourceRegionConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionParticleConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 18.04.2017.
 */
@ApplicationScoped
public class StaticGameConfigPersistence {
    public static final int FIRST_LEVEL_ID = 1;
    @Deprecated
    // GameUiControlEntity has minimal level. Should be handled with that
    public static final int MULTI_PLAYER_PLANET_LEVEL_ID = 5;
    static final int BASE_ITEM_TYPE_BULLDOZER = 180807;
    static final int BASE_ITEM_TYPE_HARVESTER = 180830;
    static final int BASE_ITEM_TYPE_ATTACKER = 180832;
    static final int BASE_ITEM_TYPE_FACTORY = 272490;
    static final int BASE_ITEM_TYPE_TOWER = 272495;
    static final int RESOURCE_ITEM_TYPE = 180829;
    static final int BOX_ITEM_TYPE = 272481;
    static final int INVENTORY_ITEM = 1;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private LevelPersistence levelPersistence;

    @Transactional
    public StaticGameConfig loadStaticGameConfig() {
        StaticGameConfig staticGameConfig = new StaticGameConfig();
        staticGameConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        staticGameConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
        staticGameConfig.setTerrainObjectConfigs(terrainElementPersistence.readTerrainObjects());
        staticGameConfig.setWaterLevel(-0.7);
        staticGameConfig.setBaseItemTypes(finalizeBaseItemTypes(itemTypePersistence.readBaseItemTypes()));
        staticGameConfig.setResourceItemTypes(finalizeResourceItemTypes(itemTypePersistence.readResourceItemTypes()));// TODO move to DB
        staticGameConfig.setBoxItemTypes(finalizeBoxItemTypes(itemTypePersistence.readBoxItemTypes()));
        staticGameConfig.setLevelConfigs(levelPersistence.read());
        staticGameConfig.setInventoryItems(setupInventoryItems()); // TODO move to DB
        return staticGameConfig;
    }

    private List<ResourceItemType> finalizeResourceItemTypes(List<ResourceItemType> resourceItemTypes) {
        finalizeSimpleResource(findResource(RESOURCE_ITEM_TYPE, resourceItemTypes));
        return resourceItemTypes;
    }

    private ResourceItemType findResource(int id, List<ResourceItemType> resourceItemTypes) {
        for (ResourceItemType resourceItemType : resourceItemTypes) {
            if (resourceItemType.getId() == id) {
                return resourceItemType;
            }
        }
        throw new IllegalArgumentException("No ResourceItemType for id: " + id);
    }

    private void finalizeSimpleResource(ResourceItemType resource) {
        resource.setTerrainType(TerrainType.LAND);
        resource.setI18Name(i18nHelper("Resource Name"));
        resource.setDescription(i18nHelper("Resource Description"));
    }

    private List<BoxItemType> finalizeBoxItemTypes(List<BoxItemType> boxItemTypes) {
        finalizeSimpleBox(findBox(BOX_ITEM_TYPE, boxItemTypes));
        return boxItemTypes;
    }

    private BoxItemType findBox(int id, List<BoxItemType> boxItemTypes) {
        for (BoxItemType boxItemType : boxItemTypes) {
            if (boxItemType.getId() == id) {
                return boxItemType;
            }
        }
        throw new IllegalArgumentException("No BoxItemType for id: " + id);
    }

    private void finalizeSimpleBox(BoxItemType boxItemType) {
        boxItemType.setTerrainType(TerrainType.LAND);
        boxItemType.setI18Name(i18nHelper("Box Name"));
        boxItemType.setDescription(i18nHelper("Box Description"));
        List<BoxItemTypePossibility> boxItemTypePossibilities = new ArrayList<>();
        boxItemTypePossibilities.add(new BoxItemTypePossibility().setPossibility(1.0).setInventoryItemId(INVENTORY_ITEM));
        boxItemType.setBoxItemTypePossibilities(boxItemTypePossibilities);
    }

    private List<BaseItemType> finalizeBaseItemTypes(List<BaseItemType> baseItemTypes) {
        finalizeBulldozer(findBaseItem(BASE_ITEM_TYPE_BULLDOZER, baseItemTypes));
        finalizeHarvester(findBaseItem(BASE_ITEM_TYPE_HARVESTER, baseItemTypes));
        finalizeAttacker(findBaseItem(BASE_ITEM_TYPE_ATTACKER, baseItemTypes));
        finalizeFactory(findBaseItem(BASE_ITEM_TYPE_FACTORY, baseItemTypes));
        finalizeTower(findBaseItem(BASE_ITEM_TYPE_TOWER, baseItemTypes));
        return baseItemTypes;
    }

    private BaseItemType findBaseItem(int id, List<BaseItemType> baseItemTypes) {
        for (BaseItemType baseItemType : baseItemTypes) {
            if (baseItemType.getId() == id) {
                return baseItemType;
            }
        }
        throw new IllegalArgumentException("No BaseItemType for id: " + id);
    }

    private void finalizeBulldozer(BaseItemType bulldozer) {
        bulldozer.setSpawnAudioId(272520);
        bulldozer.setTerrainType(TerrainType.LAND).setThumbnail(272504);
        bulldozer.setI18Name(i18nHelper("Bulldozer Name"));
        bulldozer.setDescription(i18nHelper("Bulldozer Description"));
        bulldozer.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(10.0).setAngularVelocity(Math.toRadians(60));
        bulldozer.setBuilderType(new BuilderType().setProgress(1).setRange(10).setAbleToBuildIds(Collections.singletonList(BASE_ITEM_TYPE_FACTORY)).setAnimationShape3dId(272491).setAnimationOrigin(new Vertex(1.63196, 0, 3.04829)));
        bulldozer.setBoxPickupRange(2).setExplosionParticleEmitterSequenceConfigId(2).setBuildup(30);
        bulldozer.setPrice(100).setWreckageShape3DId(272944);
    }

    private void finalizeHarvester(BaseItemType harvester) {
        harvester.setSpawnAudioId(272520).setThumbnail(284046);
        harvester.setTerrainType(TerrainType.LAND);
        harvester.setI18Name(i18nHelper("Harvester Name"));
        harvester.setDescription(i18nHelper("Harvester Description"));
        harvester.getPhysicalAreaConfig().setAcceleration(5.0).setSpeed(15.0).setAngularVelocity(Math.toRadians(60));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(3).setAnimationShape3dId(272950).setAnimationOrigin(new Vertex(2.5, 0, 1.25)));
        harvester.setBoxPickupRange(2).setExplosionParticleEmitterSequenceConfigId(2).setBuildup(20);
        harvester.setPrice(100).setWreckageShape3DId(272944);
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setSpawnAudioId(272520).setThumbnail(284045);
        attacker.setTerrainType(TerrainType.LAND);
        attacker.setI18Name(i18nHelper("Attacker Name"));
        attacker.setDescription(i18nHelper("Attacker Description"));
        attacker.getPhysicalAreaConfig().setAcceleration(5.0).setSpeed(17.0).setAngularVelocity(Math.toRadians(60));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(180837).setMuzzleFlashParticleEmitterSequenceConfigId(4).setDetonationParticleEmitterSequenceConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(-0.25, 0, 2)).setMuzzlePosition(new Vertex(1.3, 0, 0)).setShape3dMaterialId("Turret-material")));
        attacker.setBoxPickupRange(2).setExplosionParticleEmitterSequenceConfigId(2).setBuildup(15);
        attacker.setPrice(100).setWreckageShape3DId(272944);
    }

    private void finalizeFactory(BaseItemType factory) {
        factory.setSpawnAudioId(272520);
        factory.setTerrainType(TerrainType.LAND).setThumbnail(272505);
        factory.setI18Name(i18nHelper("Factory Name"));
        factory.setDescription(i18nHelper("Factory Description"));
        factory.setExplosionParticleEmitterSequenceConfigId(2).setBuildup(30);
        factory.getPhysicalAreaConfig().setFixVerticalNorm(true);
        factory.setFactoryType(new FactoryType().setProgress(1.0).setAbleToBuildIds(Arrays.asList(BASE_ITEM_TYPE_BULLDOZER, BASE_ITEM_TYPE_HARVESTER, BASE_ITEM_TYPE_ATTACKER)));
        factory.setPrice(200).setWreckageShape3DId(272943);
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionParticleConfig> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(5).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionParticleConfig> demolitionParticleConfig2s = new ArrayList<>();
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(5).setPosition(new Vertex(3, 0.47, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionParticleConfig2s));
        // Demolition 3
        List<DemolitionParticleConfig> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(-2.1, 2.0, 3.4)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(3, 0.47, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3D3s));
        factory.setDemolitionStepEffects(demolitionStepEffects);
    }

    private void finalizeTower(BaseItemType tower) {
        tower.setSpawnAudioId(272520).setThumbnail(284047);
        tower.setTerrainType(TerrainType.LAND);
        tower.setI18Name(i18nHelper("Tower"));
        tower.setDescription(i18nHelper("Verteidigungsturm"));
        tower.getPhysicalAreaConfig().setFixVerticalNorm(true);
        tower.setWeaponType(new WeaponType().setRange(20).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(40.0).setProjectileShape3DId(180837).setMuzzleFlashParticleEmitterSequenceConfigId(4).setDetonationParticleEmitterSequenceConfigId(3).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(0, 0, 0.98)).setMuzzlePosition(new Vertex(5.2, 0, 5.4)).setShape3dMaterialId("turret_001-material")));
        tower.setExplosionParticleEmitterSequenceConfigId(2).setWreckageShape3DId(272943).setBuildup(45);
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionParticleConfig> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(0, 0, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionParticleConfig> demolitionParticleConfig2s = new ArrayList<>();
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(2, 2, 2)));
        demolitionParticleConfig2s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(-2, -2, 2)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionParticleConfig2s));
        // Demolition 3
        List<DemolitionParticleConfig> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(3, 0, 1)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(0, 3, 1)));
        demolitionShape3D3s.add(new DemolitionParticleConfig().setParticleEmitterSequenceConfigId(1).setPosition(new Vertex(3, 3, 1)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionParticleConfigs(demolitionShape3D3s));
        tower.setDemolitionStepEffects(demolitionStepEffects);

    }

    private I18nString i18nHelper(String text) {
        Map<String, String> localizedStrings = new HashMap<>();
        localizedStrings.put(I18nString.DEFAULT, text);
        return new I18nString(localizedStrings);
    }


    public List<InventoryItem> setupInventoryItems() {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        inventoryItems.add(new InventoryItem().setId(StaticGameConfigPersistence.INVENTORY_ITEM).setBaseItemType(StaticGameConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setBaseItemTypeCount(3).setItemFreeRange(5).setName("3 Attacker pack").setImageId(272484));
        return inventoryItems;
    }

    private List<ResourceRegionConfig> setupResourceRegionConfigs() {
        List<ResourceRegionConfig> resourceRegionConfigs = new ArrayList<>();
        resourceRegionConfigs.add(new ResourceRegionConfig().setCount(10).setMinDistanceToItems(2).setResourceItemTypeId(StaticGameConfigPersistence.RESOURCE_ITEM_TYPE).setRegion(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(160, 140, 80, 90))));
        return resourceRegionConfigs;
    }
}
