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
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.VisualConfig;
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
    private static final int RESOURCE_ITEM_TYPE = 180829;
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
        gameEngineConfig.setBaseItemTypes(finalizeBaseItemTypes(itemTypePersistence.readBaseItemType()));// TODO mode to DB
        gameEngineConfig.setResourceItemTypes(finalizeResourceItemTypes(itemTypePersistence.readResourceItemType()));// TODO mode to DB
        gameEngineConfig.setLevelConfigs(setupLevelConfigs());  // TODO mode to DB
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        StoryboardConfig storyboardConfig = entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig(gameEngineConfig);
        storyboardConfig.setUserContext(new UserContext().setName("Emulator Name").setLevelId(1));  // TODO mode to DB
        storyboardConfig.setVisualConfig(defaultVisualConfig());  // TODO mode to DB
        completePlanetConfig(gameEngineConfig.getPlanetConfig());  // TODO mode to DB
        storyboardConfig.setSceneConfigs(setupAttack()); // TODO mode to DB
        // storyboardConfig.setSceneConfigs(setupTutorial()); // TODO mode to DB
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

    private List<BaseItemType> finalizeBaseItemTypes(List<BaseItemType> baseItemTypes) {
        finalizeBulldozer(findBaseItem(BASE_ITEM_TYPE_BULLDOZER, baseItemTypes));
        finalizeHarvester(findBaseItem(BASE_ITEM_TYPE_HARVESTER, baseItemTypes));
        finalizeAttacker(findBaseItem(BASE_ITEM_TYPE_ATTACKER, baseItemTypes));
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
    }

    private void finalizeHarvester(BaseItemType baseItemType) {
        baseItemType.setTerrainType(TerrainType.LAND);
        baseItemType.setI18Name(i18nHelper("Harvester Name"));
        baseItemType.setDescription(i18nHelper("Harvester Description"));
        baseItemType.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        baseItemType.setHarvesterType(new HarvesterType().setProgress(1).setRange(4).setAnimationShape3dId(180831).setAnimationOrigin(new Vertex(2.3051, 0, 1.7)));
    }

    private void finalizeAttacker(BaseItemType attacker) {
        attacker.setTerrainType(TerrainType.LAND);
        attacker.setI18Name(i18nHelper("Attacker Name"));
        attacker.setDescription(i18nHelper("Attacker Description"));
        attacker.getPhysicalAreaConfig().setAcceleration(40.0).setSpeed(80.0).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30));
        attacker.setWeaponType(new WeaponType().setRange(10).setDamage(1).setReloadTime(3).setDetonationRadius(1).setProjectileSpeed(17.0).setProjectileShape3DId(180837).setMuzzlePosition(new Vertex(2.9, 0, 0.85)).setMuzzleFlashClipId(180836));
    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(250));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-20).setWaterBmDepth(10).setWaterTransparency(0.65);
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
        levelConfigs.add(new LevelConfig().setLevelId(1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(itemTypeLimitation));
        levelConfigs.add(new LevelConfig().setLevelId(2).setNumber(2).setXp2LevelUp(10).setItemTypeLimitation(itemTypeLimitation));
        return levelConfigs;
    }

    private void completePlanetConfig(PlanetConfig planetConfig) {
        planetConfig.setHouseSpace(10);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
    }


    private I18nString i18nHelper(String text) {
        Map<String, String> localizedStrings = new HashMap<>();
        localizedStrings.put(I18nString.DEFAULT, text);
        return new I18nString(localizedStrings);
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
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
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
        botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(308, 173))).setNoSpawn(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
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
        StartPointConfig startPointConfig = new StartPointConfig().setBaseItemTypeId(BASE_ITEM_TYPE_BULLDOZER).setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(104, 80));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setBaseItemTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointConfig(startPointConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, Du hast soeben deinen ersten Quest bestanden")));
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
        ComparisonConfig comparisonConfig = new ComparisonConfig().setBaseItemTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(160, 70), new DecimalPosition(300, 70), new DecimalPosition(300, 200), new DecimalPosition(160, 200))))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        sceneConfigs.add(new SceneConfig().setCameraConfig(new CameraConfig().setCameraLocked(false)).setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege Deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setWait4LevelUp(true));
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
}
