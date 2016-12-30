package com.btxtech.server.persistence.impl;

import com.btxtech.server.persistence.ClipPersistence;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.StoryboardEntity;
import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.I18nString;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.TerrainType;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionShape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.DemolitionStepEffect;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 03.08.2016.
 */
@Singleton
public class StoryboardPersistenceImpl implements StoryboardPersistence {
    private static final int NPC_BOT_OUTPOST = 1;
    private static final int NPC_BOT_OUTPOST_2 = 2;
    private static final int NPC_BOT_INSTRUCTOR = 3;
    private static final int ENEMY_BOT = 4;
    private static final int BASE_ITEM_TYPE_BULLDOZER = 180807;
    private static final int BASE_ITEM_TYPE_HARVESTER = 180830;
    private static final int BASE_ITEM_TYPE_ATTACKER = 180832;
    private static final int BASE_ITEM_TYPE_FACTORY = 272490;
    private static final int BASE_ITEM_TYPE_TOWER = 272495;
    private static final int RESOURCE_ITEM_TYPE = 180829;
    private static final int BOX_ITEM_TYPE = 272481;
    private static final int INVENTORY_ITEM = 1;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;
    @Inject
    private ClipPersistence clipPersistence;

    @Override
    @Transactional
    public StoryboardConfig load() throws ParserConfigurationException, SAXException, IOException {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        gameEngineConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
        gameEngineConfig.setTerrainObjectConfigs(terrainElementPersistence.readTerrainObjects());
        gameEngineConfig.setBaseItemTypes(finalizeBaseItemTypes(itemTypePersistence.readBaseItemTypes()));// TODO mode to DB
        gameEngineConfig.setResourceItemTypes(finalizeResourceItemTypes(itemTypePersistence.readResourceItemTypes()));// TODO mode to DB
        gameEngineConfig.setBoxItemTypes(finalizeBoxItemTypes(itemTypePersistence.readBoxItemTypes()));
        gameEngineConfig.setLevelConfigs(setupLevelConfigs());  // TODO mode to DB
        gameEngineConfig.setInventoryItems(setupInventoryItems()); // TODO mode to DB
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        StoryboardConfig storyboardConfig = entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig(gameEngineConfig);
        storyboardConfig.setUserContext(new UserContext().setName("Emulator Name").setLevelId(1).setInventoryItemIds(Collections.singletonList(INVENTORY_ITEM)));  // TODO mode to DB
        storyboardConfig.setVisualConfig(defaultVisualConfig());  // TODO mode to DB
        storyboardConfig.setAudioConfig(defaultAudioConfig());  // TODO mode to DB
        storyboardConfig.setGameTipVisualConfig(defaultGameTipVisualConfig());  // TODO mode to DB
        completePlanetConfig(gameEngineConfig.getPlanetConfig());  // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupTutorial()); // TODO mode to DB
        storyboardConfig.setSceneConfigs(setupMove()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(findEnemyBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupAttack()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupTower()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupPickBox()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(killEnemyHarvester()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(kilEnemyBotBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(kilHumanBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(buildBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(harvest()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(useInventoryItem()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(demolitionVisualization()); // TODO mode to DB
        return storyboardConfig;
    }

    private GameTipVisualConfig defaultGameTipVisualConfig() {
        GameTipVisualConfig gameTipVisualConfig = new GameTipVisualConfig();
        gameTipVisualConfig.setCornerMoveDuration(1500);
        gameTipVisualConfig.setCornerMoveDistance(15);
        gameTipVisualConfig.setCornerLength(1);
        gameTipVisualConfig.setDefaultCommandShape3DId(272501);
        gameTipVisualConfig.setSelectCornerColor(new Color(0, 1, 0));
        gameTipVisualConfig.setSelectShape3DId(272499);
        gameTipVisualConfig.setOutOfViewShape3DId(272503);
        gameTipVisualConfig.setAttackCommandCornerColor(new Color(1, 0, 0));
        gameTipVisualConfig.setBaseItemPlacerCornerColor(new Color(1, 1, 0));
        gameTipVisualConfig.setBaseItemPlacerShape3DId(272499);
        gameTipVisualConfig.setGrabCommandCornerColor(new Color(0, 0, 1));
        gameTipVisualConfig.setMoveCommandCornerColor(new Color(0, 1, 0));
        gameTipVisualConfig.setToBeFinalizedCornerColor(new Color(1, 1, 0));
        gameTipVisualConfig.setWestLeftMouseGuiImageId(272506);
        gameTipVisualConfig.setSouthLeftMouseGuiImageId(272507);
        gameTipVisualConfig.setDirectionShape3DId(272503);
        gameTipVisualConfig.setSplashImageId(272508);
        return gameTipVisualConfig;
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
        bulldozer.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        bulldozer.setBuilderType(new BuilderType().setProgress(1).setRange(10).setAbleToBuild(Collections.singletonList(BASE_ITEM_TYPE_FACTORY)).setAnimationShape3dId(272491).setAnimationOrigin(new Vertex(2.3051, 0, 1.7)));
        bulldozer.setBoxPickupRange(2).setExplosionClipId(272485);
    }

    private void finalizeHarvester(BaseItemType harvester) {
        harvester.setSpawnAudioId(272520);
        harvester.setTerrainType(TerrainType.LAND);
        harvester.setI18Name(i18nHelper("Harvester Name"));
        harvester.setDescription(i18nHelper("Harvester Description"));
        harvester.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4).setAnimationShape3dId(180831).setAnimationOrigin(new Vertex(2.3051, 0, 1.7)));
        harvester.setBoxPickupRange(2).setExplosionClipId(272485).setBuildup(2);
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setSpawnAudioId(272520);
        attacker.setTerrainType(TerrainType.LAND);
        attacker.setI18Name(i18nHelper("Attacker Name"));
        attacker.setDescription(i18nHelper("Attacker Description"));
        attacker.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(180837).setMuzzleFlashClipId(180836).setDetonationClipId(180842).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(-0.25, 0, 2)).setMuzzlePosition(new Vertex(1.3, 0, 0)).setShape3dMaterialId("Turret-material")));
        attacker.setBoxPickupRange(2).setExplosionClipId(272485);
    }

    private void finalizeFactory(BaseItemType factory) {
        factory.setSpawnAudioId(272520);
        factory.setTerrainType(TerrainType.LAND).setThumbnail(272505);
        factory.setI18Name(i18nHelper("Factory Name"));
        factory.setDescription(i18nHelper("Factory Description"));
        factory.setExplosionClipId(272485).setBuildup(2);
        factory.getPhysicalAreaConfig().setFixVerticalNorm(true);
        factory.setFactoryType(new FactoryType().setProgress(1.0).setAbleToBuildId(Arrays.asList(BASE_ITEM_TYPE_BULLDOZER, BASE_ITEM_TYPE_HARVESTER, BASE_ITEM_TYPE_ATTACKER)));
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionShape3D> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(-2.1, 0.0, 3.4)));
        demolitionShape3Ds1.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(2.8, 2.2, 2.0)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionShape3D> demolitionShape3D2s = new ArrayList<>();
        demolitionShape3D2s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(2, 2, 2)));
        demolitionShape3D2s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(-2, -2, 2)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3D2s));
        // Demolition 3
        List<DemolitionShape3D> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(3, 0, 1)));
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(0, 3, 1)));
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(3, 3, 1)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3D3s));
        factory.setDemolitionStepEffects(demolitionStepEffects);
    }

    private void finalizeTower(BaseItemType tower) {
        tower.setSpawnAudioId(272520);
        tower.setTerrainType(TerrainType.LAND);
        tower.setI18Name(i18nHelper("Tower"));
        tower.setDescription(i18nHelper("Verteidigungsturm"));
        tower.getPhysicalAreaConfig().setFixVerticalNorm(true);
        tower.setWeaponType(new WeaponType().setRange(20).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(40.0).setProjectileShape3DId(180837).setMuzzleFlashClipId(180836).setDetonationClipId(180842).setTurretType(new TurretType().setAngleVelocity(Math.toRadians(120)).setTorrentCenter(new Vertex(0, 0, 0.98)).setMuzzlePosition(new Vertex(5.2, 0, 5.4)).setShape3dMaterialId("turret_001-material")));
        tower.setExplosionClipId(272485);
        List<DemolitionStepEffect> demolitionStepEffects = new ArrayList<>();
        // Demolition 1
        List<DemolitionShape3D> demolitionShape3Ds1 = new ArrayList<>();
        demolitionShape3Ds1.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(0, 0, 3)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3Ds1));
        // Demolition 2
        List<DemolitionShape3D> demolitionShape3D2s = new ArrayList<>();
        demolitionShape3D2s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(2, 2, 2)));
        demolitionShape3D2s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(-2, -2, 2)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3D2s));
        // Demolition 3
        List<DemolitionShape3D> demolitionShape3D3s = new ArrayList<>();
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(3, 0, 1)));
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(0, 3, 1)));
        demolitionShape3D3s.add(new DemolitionShape3D().setShape3DId(272511).setPosition(new Vertex(3, 3, 1)));
        demolitionStepEffects.add(new DemolitionStepEffect().setDemolitionShape3Ds(demolitionShape3D3s));
        tower.setDemolitionStepEffects(demolitionStepEffects);

    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(-27)).setShadowRotationY(Math.toRadians(0));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-2).setWaterBmDepth(10).setWaterTransparency(0.65).setWaterBmId(272480).setWaterBmDepth(20).setWaterBmScale(0.01);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setRotationX(Math.toRadians(-20));
        lightConfig.setRotationY(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
        visualConfig.setWaterLightConfig(lightConfig);
        visualConfig.setShape3Ds(shape3DPersistence.getShape3Ds());
        visualConfig.setClipConfigs(clipPersistence.readClipConfigs());
        visualConfig.setBaseItemDemolitionCuttingImageId(170418).setBaseItemDemolitionLookUpImageId(272510);
        return visualConfig;
    }

    private AudioConfig defaultAudioConfig() {
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setDialogOpened(272514);
        audioConfig.setDialogClosed(272515);
        audioConfig.setOnQuestActivated(272516);
        audioConfig.setOnQuestPassed(272517);
        audioConfig.setOnLevelUp(272518);
        audioConfig.setOnBoxPicked(272519);
        audioConfig.setOnSelectionCleared(272525);
        audioConfig.setOnOwnMultiSelection(272526);
        audioConfig.setOnOwnSingleSelection(272527);
        audioConfig.setOnTargetSelection(272528);
        audioConfig.setOnCommandSent(272529);
        return audioConfig;
    }


    private List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> level1Limitation = new HashMap<>();
        level1Limitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        levelConfigs.add(new LevelConfig().setLevelId(1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(level1Limitation));
        Map<Integer, Integer> level2Limitation = new HashMap<>();
        level2Limitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        level2Limitation.put(BASE_ITEM_TYPE_ATTACKER, 5);
        levelConfigs.add(new LevelConfig().setLevelId(2).setNumber(2).setXp2LevelUp(13).setItemTypeLimitation(level2Limitation));
        Map<Integer, Integer> level3Limitation = new HashMap<>();
        level3Limitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        level3Limitation.put(BASE_ITEM_TYPE_ATTACKER, 5);
        level3Limitation.put(BASE_ITEM_TYPE_HARVESTER, 1);
        level3Limitation.put(BASE_ITEM_TYPE_FACTORY, 1);
        levelConfigs.add(new LevelConfig().setLevelId(3).setNumber(3).setXp2LevelUp(30).setItemTypeLimitation(level3Limitation));
        Map<Integer, Integer> level4Limitation = new HashMap<>();
        level4Limitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        level4Limitation.put(BASE_ITEM_TYPE_ATTACKER, 5);
        level4Limitation.put(BASE_ITEM_TYPE_HARVESTER, 1);
        level4Limitation.put(BASE_ITEM_TYPE_FACTORY, 100);
        levelConfigs.add(new LevelConfig().setLevelId(4).setNumber(4).setXp2LevelUp(50).setItemTypeLimitation(level4Limitation));
        return levelConfigs;
    }

    public List<InventoryItem> setupInventoryItems() {
        List<InventoryItem> inventoryItems = new ArrayList<>();
        inventoryItems.add(new InventoryItem().setId(INVENTORY_ITEM).setBaseItemType(BASE_ITEM_TYPE_ATTACKER).setBaseItemTypeCount(3).setItemFreeRange(5).setName("3 Attacker pack").setImageId(272484));
        return inventoryItems;
    }

    private void completePlanetConfig(PlanetConfig planetConfig) {
        planetConfig.setHouseSpace(10);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        itemTypeLimitation.put(BASE_ITEM_TYPE_ATTACKER, 5);
        itemTypeLimitation.put(BASE_ITEM_TYPE_HARVESTER, 5);
        itemTypeLimitation.put(BASE_ITEM_TYPE_FACTORY, 100);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
        planetConfig.setWaterLevel(-0.7);
    }


    private I18nString i18nHelper(String text) {
        Map<String, String> localizedStrings = new HashMap<>();
        localizedStrings.put(I18nString.DEFAULT, text);
        return new I18nString(localizedStrings);
    }

    // Move and tip  -----------------------------------------------------------------------------
    private List<SceneConfig> setupMove() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(104, 80));
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(startConditionConfig).setXp(1).setPassedMessage("Gratuliere, Du hast soeben deinen ersten Quest bestanden")));
        // Move quest
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(160, 70), new DecimalPosition(300, 70), new DecimalPosition(300, 200), new DecimalPosition(160, 200))))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.MOVE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(200, 100));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege Deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setWait4LevelUpDialog(true));

        return sceneConfigs;
    }

    // Tower -----------------------------------------------------------------------------
    private List<SceneConfig> setupTower() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Tower bot
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(75, 246))).setNoSpawn(true).setNoRebuild(true));
        // botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(75, 246))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));

        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setBotConfigs(botConfigs).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // User InventoryItem -----------------------------------------------------------------------------
    private List<SceneConfig> useInventoryItem() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Start").setConditionConfig(startConditionConfig)).setWait4QuestPassedDialog(true));
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.INVENTORY_ITEM_PLACED).setComparisonConfig(new ComparisonConfig().setCount(1));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SPAN_INVENTORY_ITEM);
        gameTipConfig.setInventoryItemId(INVENTORY_ITEM);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(50, 300));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Build base -----------------------------------------------------------------------------
    private List<SceneConfig> buildBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        Map<Integer, Integer> startTypeCount = new HashMap<>();
        startTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(startTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Start").setConditionConfig(startConditionConfig)).setWait4QuestPassedDialog(true));
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_FACTORY, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig buildGameTipConfig = new GameTipConfig();
        buildGameTipConfig.setTip(GameTipConfig.Tip.BUILD);
        buildGameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER);
        buildGameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_FACTORY);
        buildGameTipConfig.setTerrainPositionHint(new DecimalPosition(54, 260));
        sceneConfigs.add(new SceneConfig().setGameTipConfig(buildGameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Platziere deinen Bulldozer und baue eine Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        // Build Harvester Quest
        buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig factoryGameTipConfig = new GameTipConfig();
        factoryGameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        factoryGameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY);
        factoryGameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_HARVESTER);
        sceneConfigs.add(new SceneConfig().setGameTipConfig(factoryGameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue eine Harvester in deiner Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Build base -----------------------------------------------------------------------------
    private List<SceneConfig> harvest() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Player base place
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(64, 219)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(77, 220)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(94, 225)).setRotationZ(Math.toRadians(160)));
        // Harvest quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100));
        QuestConfig questConfig = new QuestConfig().setTitle("Sammle").setDescription("Sammle razarion um eine Armee zu bauen").setConditionConfig(conditionConfig);

        sceneConfigs.add(new SceneConfig().setStartPointPlacerConfig(baseItemPlacerConfig).setCameraConfig(cameraConfig).setResourceItemTypePositions(resourceItemTypePositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Kill human base -----------------------------------------------------------------------------
    private List<SceneConfig> kilHumanBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(243, 120));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setConditionConfig(conditionConfig).setTitle("Platzieren").setDescription("Platzieren")).setWait4QuestPassedDialog(true));
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Kill bot command
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        botKillHumanCommandConfigs.add(new BotKillHumanCommandConfig().setBotId(ENEMY_BOT).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        // Camera
        sceneConfigs.add(new SceneConfig().setBotConfigs(botConfigs).setBotKillHumanCommandConfigs(botKillHumanCommandConfigs));
        return sceneConfigs;
    }

    // Kill enemy bot base -----------------------------------------------------------------------------
    private List<SceneConfig> kilEnemyBotBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        addNpcBot(sceneConfigs);
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Kill bot command
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigss = new ArrayList<>();
        botKillOtherBotCommandConfigss.add(new BotKillOtherBotCommandConfig().setBotId(ENEMY_BOT).setTargetBotId(NPC_BOT_OUTPOST).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        // Camera
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigss));
        return sceneConfigs;
    }

    // Kill enemy harvester -----------------------------------------------------------------------------
    private List<SceneConfig> killEnemyHarvester() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Resources
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(244, 187)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(264, 182)).setRotationZ(Math.toRadians(80)));
        // Enemy target
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(250, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(244, 187))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(264, 182))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        // Camera
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(243, 80));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));

        Map<Integer, Integer> killItemTypeCount = new HashMap<>();
        killItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        sceneConfigs.add(new SceneConfig().setQuestConfig(new QuestConfig().setConditionConfig(conditionConfig).setTitle("Platzieren").setDescription("Platzieren")).setWait4QuestPassedDialog(true).setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs).setResourceItemTypePositions(resourceItemTypePositions).setStartPointPlacerConfig(baseItemPlacerConfig));
        sceneConfigs.add(new SceneConfig().setQuestConfig(new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(killItemTypeCount))).setTitle("Kill").setDescription("Kill 2")).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Find Pick Box -----------------------------------------------------------------------------
    private List<SceneConfig> setupPickBox() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(BOX_ITEM_TYPE).setPosition(new DecimalPosition(110, 80)));
        // Camera
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);

        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBoxItemPositions(boxItemPositions));
        addUserSpawnScene(sceneConfigs);

        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        sceneConfigs.add(new SceneConfig().setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Find Enemy Base -----------------------------------------------------------------------------
    private List<SceneConfig> findEnemyBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Bot Attacker
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> attackerEnragement = new ArrayList<>();
        List<BotItemConfig> attackerBotItems = new ArrayList<>();
        attackerBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(305, 175))).setNoSpawn(true));
        attackerEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerBotItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragement).setName("Kenny").setNpc(false));
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(300, 170, 10, 10)).setXp(1).setPassedMessage("Gratuliere, Du hast die gegnerische Basis gefunden");
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SCROLL);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(305, 175));
        // div
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Attack -----------------------------------------------------------------------------
    private List<SceneConfig> setupAttack() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        List<BotConfig> botConfigs = new ArrayList<>();
        // Bot Target
        List<BotEnragementStateConfig> targetEnragement = new ArrayList<>();
        List<BotItemConfig> targetBotItems = new ArrayList<>();
        targetBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(100, 80))).setNoSpawn(true).setNoRebuild(true));
        targetEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(targetBotItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_INSTRUCTOR).setActionDelay(3000).setBotEnragementStateConfigs(targetEnragement).setName("Kenny").setNpc(true));
        // Bot Attacker
        List<BotEnragementStateConfig> attackerEnragement = new ArrayList<>();
        List<BotItemConfig> attackerBotItems = new ArrayList<>();
        attackerBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(90, 80))).setNoSpawn(true).setNoRebuild(true));
        attackerEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerBotItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragement).setName("Kenny").setNpc(false));
        // Attack command
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(100, 80))).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER));
        // div
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setBotAttackCommandConfigs(botAttackCommandConfigs));
        return sceneConfigs;
    }

    // Demolition Visualization -----------------------------------------------------------------------------
    private List<SceneConfig> demolitionVisualization() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(70, 170)).setCameraLocked(false);
        // Bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(75, 246))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));

        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Tutorial -----------------------------------------------------------------------------
    private List<SceneConfig> setupTutorial() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Level 1
        addResources(sceneConfigs);
        addNpcBot(sceneConfigs);
        addEnemyBot(sceneConfigs);
        addScrollOverTerrain(sceneConfigs);
        addBotSpawnScene(sceneConfigs);
        addUserSpawnScene(sceneConfigs);
        addBotMoveScene(sceneConfigs);
        addScrollToOwnScene(sceneConfigs);
        addUserMoveScene(sceneConfigs);
        // Level 2
        addNpcHarvestAttack(sceneConfigs);
        addFindEnemyBase(sceneConfigs);
        addPickBoxTask(sceneConfigs);
        addBoxSpawnTask(sceneConfigs);
        addAttackTask(sceneConfigs);
        // Level 3
        addEnemyKillTask(sceneConfigs);
        addNpcEscapeTask(sceneConfigs);
        addUserSpawnScene2(sceneConfigs);
        addBuildFactoryTask(sceneConfigs);
        addFactorizeHarvesterTask(sceneConfigs);
        addHarvestTask(sceneConfigs);
        addHarvestExplanationTask(sceneConfigs);
        // Level 4
        addBuildViperTask(sceneConfigs);
        addNpcAttackTowerCommand(sceneConfigs);
        addNpcTooWeakCommand(sceneConfigs);
        addBuildViperTask2(sceneConfigs);
        addKillTower(sceneConfigs);
        return sceneConfigs;
    }

    private void addNpcBot(List<SceneConfig> sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(228, 140))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(221, 144))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(207, 115))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(232, 85))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(276, 87))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(260, 94))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(260, 115))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(248, 123))).setNoSpawn(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(NPC_BOT_OUTPOST).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(244, 187))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(NPC_BOT_OUTPOST).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(264, 182))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        sceneConfigs.add(new SceneConfig().setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
    }

    private void addEnemyBot(List<SceneConfig> sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(288, 180))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(281, 184))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(267, 155))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(292, 135))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(316, 137))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(330, 144))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(340, 165))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(305, 175))).setNoSpawn(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_TOWER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(175, 270))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Razar Industries").setNpc(false));
        sceneConfigs.add(new SceneConfig().setBotConfigs(botConfigs));
    }

    private void addResources(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig();
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        // Outpost
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(235, 199)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(254, 200)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(244, 187)).setRotationZ(Math.toRadians(160)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(264, 182)).setRotationZ(Math.toRadians(240)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(276, 211)).setRotationZ(Math.toRadians(320)));
        // Player base place
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(64, 219)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(77, 220)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(94, 225)).setRotationZ(Math.toRadians(160)));

        sceneConfig.setResourceItemTypePositions(resourceItemTypePositions);
        sceneConfigs.add(sceneConfig);
    }

    private void addScrollOverTerrain(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setIntroText("Willkommen Kommandant, Razarion Industries betreibt Raubbau auf diesem Planeten. Ihre Aufgabe ist es, Razarion Industries von diesem Planeten zu vertreiben.");
        sceneConfig.setCameraConfig(new CameraConfig().setFromPosition(new DecimalPosition(326, 290)).setToPosition(new DecimalPosition(104, 32)).setSpeed(100.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void addBotSpawnScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(true);
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(104, 80))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_INSTRUCTOR).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setIntroText("Kenny unterstützt Dich dabei. Er wird sich gleich auf die Planetenoberfläche beamen.").setDuration(8000));
    }

    private void addUserSpawnScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(true);
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(128, 80));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(128, 80));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setCameraConfig(cameraConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, Du hast soeben deinen ersten Quest bestanden")));
    }

    private void addBotMoveScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(204, 52)).setSpeed(50.0).setCameraLocked(true);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(NPC_BOT_INSTRUCTOR).setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setTargetPosition(new DecimalPosition(204, 100)));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Folge mir zum Vorposten"));
    }

    private void addScrollToOwnScene(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setIntroText("Fahre deine Einheit zum Vorposten");
        sceneConfig.setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(164, 32)).setSpeed(50.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void addUserMoveScene(List<SceneConfig> sceneConfigs) {
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(160, 70), new DecimalPosition(300, 70), new DecimalPosition(300, 200), new DecimalPosition(160, 200))))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.MOVE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(200, 100));

        sceneConfigs.add(new SceneConfig().setCameraConfig(new CameraConfig().setCameraLocked(false)).setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege Deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true));
    }

    private void addNpcHarvestAttack(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig();
        //sceneConfig.setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(250, 130)).setSpeed(50.0).setCameraLocked(false));
        sceneConfig.setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(250, 130)).setCameraLocked(false));
        sceneConfigs.add(sceneConfig);
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(BASE_ITEM_TYPE_HARVESTER).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(233, 178, 22, 19))));
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(BASE_ITEM_TYPE_HARVESTER).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(255, 173, 22, 19))));
        sceneConfig.setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(10000).setIntroText("Hilfe wir werden angegriffen");
        sceneConfigs.add(sceneConfig);
    }

    private void addFindEnemyBase(List<SceneConfig> sceneConfigs) {
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setXp(1).setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(300, 170, 10, 10)).setXp(1).setPassedMessage("Gratuliere, Du hast die gegnerische Basis gefunden");
        // div
        CameraConfig cameraConfig = new CameraConfig().setCameraLocked(false);
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(244, 187))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(264, 182))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SCROLL);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(305, 175));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setCameraConfig(cameraConfig).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
    }

    private void addPickBoxTask(List<SceneConfig> sceneConfigs) {
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(BOX_ITEM_TYPE).setPosition(new DecimalPosition(110, 80)));
        // Pick box quest
        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.PICK_BOX);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setToGrabItemTypeId(BOX_ITEM_TYPE);

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setBoxItemPositions(boxItemPositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
    }

    private void addBoxSpawnTask(List<SceneConfig> sceneConfigs) {
        // Use inventory item quest
        // ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.INVENTORY_ITEM_PLACED).setComparisonConfig(new ComparisonConfig().setCount(1));
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(1));
        BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig = new BotRemoveOwnItemCommandConfig().setBotId(ENEMY_BOT).setBaseItemType2RemoveId(BASE_ITEM_TYPE_ATTACKER);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SPAN_INVENTORY_ITEM);
        gameTipConfig.setInventoryItemId(INVENTORY_ITEM);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(232, 170));
        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setXp(1).setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true).setBotRemoveOwnItemCommandConfigs(Collections.singletonList(botRemoveOwnItemCommandConfig)));
    }

    private void addAttackTask(List<SceneConfig> sceneConfigs) {
        // Attack quest
        Map<Integer, Integer> attackItemTypeCount = new HashMap<>();
        attackItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        QuestConfig questConfig = new QuestConfig().setXp(10).setTitle("Zerstöre die Abbaufahrzeuge").setDescription("Greiffe Razarion insudtries an und zerstöre die Abbaufahrzeuge").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(attackItemTypeCount)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(BASE_ITEM_TYPE_ATTACKER);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPolygon2D(new Rectangle2D(223, 174, 35, 35).toPolygon()));

        sceneConfigs.add(new SceneConfig().setQuestConfig(questConfig).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true));
    }

    private void addEnemyKillTask(List<SceneConfig> sceneConfigs) {
        // Kill bot command
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigss = new ArrayList<>();
        botKillOtherBotCommandConfigss.add(new BotKillOtherBotCommandConfig().setBotId(ENEMY_BOT).setTargetBotId(NPC_BOT_OUTPOST).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        // Kill human command
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        botKillHumanCommandConfigs.add(new BotKillHumanCommandConfig().setBotId(ENEMY_BOT).setDominanceFactor(2).setAttackerBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        sceneConfigs.add(new SceneConfig().setBotKillHumanCommandConfigs(botKillHumanCommandConfigs)/*.setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigss)*/.setIntroText("Hilfe, Razar Industries greift uns an").setDuration(4000));
    }

    private void addNpcEscapeTask(List<SceneConfig> sceneConfigs) {
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(NPC_BOT_INSTRUCTOR).setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setTargetPosition(new DecimalPosition(400, 100)));
        sceneConfigs.add(new SceneConfig().setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Baue dich neu auf und zerstöre Razar Industries. Ich flüchte zum nächsten Rebellen PLanet.").setDuration(4000));
    }

    private void addUserSpawnScene2(List<SceneConfig> sceneConfigs) {
        // Bot NPC_BOT_OUTPOST_2
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(130, 270))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST_2).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(60, 250));

        // Kill NPC_BOT_INSTRUCTOR
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotId(NPC_BOT_INSTRUCTOR));
        // Build factory Quest
        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Basis").setDescription("Platziere deinen Bulldozer und baue Basis auf um Razarion Industries zu besiegen.").setPassedMessage("Ist dieser Dialg notwendig`?").setConditionConfig(conditionConfig).setXp(0)).setWait4QuestPassedDialog(true).setKillBotCommandConfigs(killBotCommandConfigs).setBotConfigs(botConfigs));
    }

    private void addBuildFactoryTask(List<SceneConfig> sceneConfigs) {
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_FACTORY, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.BUILD);
        gameTipConfig.setActor(BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(54, 260));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Baue eine Fabrik mit deinem Bulldozer").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addFactorizeHarvesterTask(List<SceneConfig> sceneConfigs) {
        // Build Harvester Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Kill NPC_BOT_OUTPOST
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotId(NPC_BOT_OUTPOST));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_HARVESTER);

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue ein Harvester in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true).setKillBotCommandConfigs(killBotCommandConfigs));
    }

    private void addHarvestTask(List<SceneConfig> sceneConfigs) {
        // Harvest quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.HARVEST);
        gameTipConfig.setActor(BASE_ITEM_TYPE_HARVESTER);
        gameTipConfig.setToGrabItemTypeId(RESOURCE_ITEM_TYPE);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(64, 219)));
        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Sammle Razarion").setDescription("Sammle Razarion um eine Armee zu bauen").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addHarvestExplanationTask(List<SceneConfig> sceneConfigs) {
        // Harvest explanation
        sceneConfigs.add(new SceneConfig().setIntroText("Du brauchst viel Razarion um eine Armee zu bauen").setDuration(3000));
    }

    private void addBuildViperTask(List<SceneConfig> sceneConfigs) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_ATTACKER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_ATTACKER);

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue ein Viper in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addNpcAttackTowerCommand(List<SceneConfig> sceneConfigs) {
        // Attack command
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(NPC_BOT_OUTPOST_2).setTargetItemTypeId(BASE_ITEM_TYPE_TOWER).setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(175, 270))).setActorItemTypeId(BASE_ITEM_TYPE_ATTACKER));
        sceneConfigs.add(new SceneConfig().setIntroText("Komm, greiffen wir an!").setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(3000).setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(175, 190))));
    }

    private void addNpcTooWeakCommand(List<SceneConfig> sceneConfigs) {
        // Attack command
        sceneConfigs.add(new SceneConfig().setIntroText("Der Turm ist zu stark, wir brauchen eine grössere Armee").setDuration(2000).setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(175, 190))));
    }

    private void addBuildViperTask2(List<SceneConfig> sceneConfigs) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_ATTACKER, 2);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(BASE_ITEM_TYPE_ATTACKER);

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue zwei Vipers in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addKillTower(List<SceneConfig> sceneConfigs) {
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_TOWER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(BASE_ITEM_TYPE_ATTACKER);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(175, 270)));

        sceneConfigs.add(new SceneConfig().setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Zerstöre Turm").setDescription("Nimm deine 3 Vipers und zerstöre den Turm").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

}
