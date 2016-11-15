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
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BotRemoveOwnItemCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.CameraConfig;
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
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
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
    private static final int NPC_BOT_INSTRUCTOR = 2;
    private static final int ENEMY_BOT = 3;
    private static final int BASE_ITEM_TYPE_BULLDOZER = 180807;
    private static final int BASE_ITEM_TYPE_HARVESTER = 180830;
    private static final int BASE_ITEM_TYPE_ATTACKER = 180832;
    private static final int BASE_ITEM_TYPE_FACTORY = 272490;
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
        completePlanetConfig(gameEngineConfig.getPlanetConfig());  // TODO mode to DB
        storyboardConfig.setSceneConfigs(setupTutorial()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(findEnemyBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupAttack()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupPickBox()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(killEnemyHarvester()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(kilEnemyBotBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(kilHumanBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(buildBase()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(useInventoryItem()); // TODO mode to DB
        return storyboardConfig;
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

    private void finalizeBulldozer(BaseItemType baseItemType) {
        baseItemType.setTerrainType(TerrainType.LAND);
        baseItemType.setI18Name(i18nHelper("Bulldozer Name"));
        baseItemType.setDescription(i18nHelper("Bulldozer Description"));
        baseItemType.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        baseItemType.setBuilderType(new BuilderType().setProgress(1).setRange(10).setAbleToBuild(Collections.singletonList(BASE_ITEM_TYPE_FACTORY)).setAnimationShape3dId(272491).setAnimationOrigin(new Vertex(2.3051, 0, 1.7)));
        baseItemType.setBoxPickupRange(2).setExplosionClipId(272485);
    }

    private void finalizeHarvester(BaseItemType baseItemType) {
        baseItemType.setTerrainType(TerrainType.LAND);
        baseItemType.setI18Name(i18nHelper("Harvester Name"));
        baseItemType.setDescription(i18nHelper("Harvester Description"));
        baseItemType.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        baseItemType.setHarvesterType(new HarvesterType().setProgress(1).setRange(4).setAnimationShape3dId(180831).setAnimationOrigin(new Vertex(2.3051, 0, 1.7)));
        baseItemType.setBoxPickupRange(2).setExplosionClipId(272485).setBuildup(2);
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setTerrainType(TerrainType.LAND);
        attacker.setI18Name(i18nHelper("Attacker Name"));
        attacker.setDescription(i18nHelper("Attacker Description"));
        attacker.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(180837).setMuzzlePosition(new Vertex(2.9, 0, 0.85)).setMuzzleFlashClipId(180836).setDetonationClipId(180842));
        attacker.setBoxPickupRange(2).setExplosionClipId(272485);
    }

    private void finalizeFactory(BaseItemType factory) {
        factory.setTerrainType(TerrainType.LAND);
        factory.setI18Name(i18nHelper("Factory Name"));
        factory.setDescription(i18nHelper("Factory Description"));
        factory.setExplosionClipId(272485).setBuildup(2);
        factory.setFactoryType(new FactoryType().setProgress(1.0).setAbleToBuildId(Arrays.asList(BASE_ITEM_TYPE_BULLDOZER, BASE_ITEM_TYPE_HARVESTER)));
    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(250));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-2).setWaterBmDepth(10).setWaterTransparency(0.65).setWaterBmId(272480).setWaterBmDepth(20).setWaterBmScale(0.01);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setRotationX(Math.toRadians(-20));
        lightConfig.setRotationY(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
        visualConfig.setWaterLightConfig(lightConfig);
        visualConfig.setShape3Ds(shape3DPersistence.getShape3Ds());
        visualConfig.setClipConfigs(clipPersistence.readClipConfigs());
        return visualConfig;
    }

    private List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        itemTypeLimitation.put(BASE_ITEM_TYPE_ATTACKER, 5);
        itemTypeLimitation.put(BASE_ITEM_TYPE_HARVESTER, 5);
        itemTypeLimitation.put(BASE_ITEM_TYPE_FACTORY, 100);
        levelConfigs.add(new LevelConfig().setLevelId(1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(itemTypeLimitation));
        levelConfigs.add(new LevelConfig().setLevelId(2).setNumber(2).setXp2LevelUp(10).setItemTypeLimitation(itemTypeLimitation));
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

    // User InventoryItem -----------------------------------------------------------------------------
    private List<SceneConfig> useInventoryItem() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.INVENTORY_ITEM_PLACED).setComparisonConfig(new ComparisonConfig().setCount(1));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Build base -----------------------------------------------------------------------------
    private List<SceneConfig> buildBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_FACTORY, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Platziere deinen Bulldozer und baue eine Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        // Build Harvester Quest
        buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue eine Harvester in deiner Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
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
        List<BotConfig> botConfigs = new ArrayList<>();
        // Bot Attacker
        List<BotEnragementStateConfig> attackerEnragement = new ArrayList<>();
        List<BotItemConfig> attackerBotItems = new ArrayList<>();
        attackerBotItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(305, 175))).setNoSpawn(true));
        attackerEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerBotItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragement).setName("Kenny").setNpc(false));
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(300, 170, 10, 10)).setXp(1).setPassedMessage("Gratuliere, Du hast die gegnerische Basis gefunden");
        // div
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setScrollUiQuest(scrollUiQuest));
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

    // Tutorial -----------------------------------------------------------------------------
    private List<SceneConfig> setupTutorial() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        addResources(sceneConfigs);
        addNpcBot(sceneConfigs);
        addEnemyBot(sceneConfigs);
        addScrollOverTerrain(sceneConfigs);
        addBotSpawnScene(sceneConfigs);
        addUserSpawnScene(sceneConfigs);
        addBotMoveScene(sceneConfigs);
        addScrollToOwnScene(sceneConfigs);
        addUserMoveScene(sceneConfigs);
        addNpcHarvestAttack(sceneConfigs);
        addFindEnemyBase(sceneConfigs);
        addPickBoxTask(sceneConfigs);
        addBoxSpawnTask(sceneConfigs);
        addAttackTask(sceneConfigs);
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
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Razar Industries").setNpc(false));
        sceneConfigs.add(new SceneConfig().setBotConfigs(botConfigs));
    }

    private void addResources(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig();
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(235, 199)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(254, 200)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(244, 187)).setRotationZ(Math.toRadians(160)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(264, 182)).setRotationZ(Math.toRadians(240)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(276, 211)).setRotationZ(Math.toRadians(320)));
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
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setBaseItemCount(1).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(104, 80));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, Du hast soeben deinen ersten Quest bestanden")));
    }

    private void addBotMoveScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(204, 52)).setSpeed(50.0).setCameraLocked(false);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(NPC_BOT_INSTRUCTOR).setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setDecimalPosition(new DecimalPosition(204, 100)));
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
        sceneConfigs.add(new SceneConfig().setCameraConfig(new CameraConfig().setCameraLocked(false)).setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege Deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setWait4LevelUpDialog(true));
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
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(300, 170, 10, 10)).setXp(1).setPassedMessage("Gratuliere, Du hast die gegnerische Basis gefunden");
        // div
        CameraConfig cameraConfig = new CameraConfig().setCameraLocked(false);
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(244, 187))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(264, 182))).setHarvesterItemTypeId(BASE_ITEM_TYPE_HARVESTER));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
    }

    private void addPickBoxTask(List<SceneConfig> sceneConfigs) {
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(BOX_ITEM_TYPE).setPosition(new DecimalPosition(110, 80)));
        // Pick box quest
        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));

        sceneConfigs.add(new SceneConfig().setBoxItemPositions(boxItemPositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
    }

    private void addBoxSpawnTask(List<SceneConfig> sceneConfigs) {
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.INVENTORY_ITEM_PLACED).setComparisonConfig(new ComparisonConfig().setCount(1));
        BotRemoveOwnItemCommandConfig botRemoveOwnItemCommandConfig = new BotRemoveOwnItemCommandConfig().setBotId(ENEMY_BOT).setBaseItemType2RemoveId(BASE_ITEM_TYPE_ATTACKER);
        sceneConfigs.add(new SceneConfig().setQuestConfig(new QuestConfig().setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true).setBotRemoveOwnItemCommandConfigs(Collections.singletonList(botRemoveOwnItemCommandConfig)));
    }

    private void addAttackTask(List<SceneConfig> sceneConfigs) {
        // Attack quest
        Map<Integer, Integer> attackItemTypeCount = new HashMap<>();
        attackItemTypeCount.put(BASE_ITEM_TYPE_HARVESTER, 1);
        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Zerstöre die Abbaufahrzeuge").setDescription("Greiffe Razarion insudtries an und zerstöre die Abbaufahrzeuge").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(attackItemTypeCount)));
        sceneConfigs.add(new SceneConfig().setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
    }

}
