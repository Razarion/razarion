package com.btxtech.server.persistence;

import com.btxtech.server.gameengine.GameEngineService;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.dto.BaseItemPlacerConfig;
import com.btxtech.shared.dto.BotAttackCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotKillHumanCommandConfig;
import com.btxtech.shared.dto.BotKillOtherBotCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.BoxItemPosition;
import com.btxtech.shared.dto.GameTipConfig;
import com.btxtech.shared.dto.GameTipVisualConfig;
import com.btxtech.shared.dto.GameUiControlConfig;
import com.btxtech.shared.dto.KillBotCommandConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.ResourceItemPosition;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.ScrollUiQuest;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.dto.WaterConfig;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.config.ComparisonConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionConfig;
import com.btxtech.shared.gameengine.datatypes.config.ConditionTrigger;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
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
public class GameUiControlConfigPersistence {
    private static final int NPC_BOT_OUTPOST = 1;
    private static final int NPC_BOT_OUTPOST_2 = 2;
    private static final int NPC_BOT_INSTRUCTOR = 3;
    private static final int ENEMY_BOT = 4;
    public static final int PLANET_BOT_1 = 5;
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private GameEngineConfigPersistence gameEngineConfigPersistence;
    @Inject
    private GameEngineService gameEngineService;

    // TODO DB Migration
    // TODO create GAME_UI_CONTROL_CONFIG with id 2, planet 2
    // TODO create PLANET with id 2

    @Transactional
    public GameUiControlConfig load(UserContext userContext) throws ParserConfigurationException, SAXException, IOException {
        GameEngineConfig gameEngineConfig = gameEngineConfigPersistence.load4Client();

        int levelNumber = getLevelNumber(userContext.getLevelId());
        // TODO move to DB
        GameUiControlConfig gameUiControlConfig;
        if (levelNumber >= 5) {
            // Multiplayer
            gameUiControlConfig = getGameUiControlConfig4Level(2).toGameUiControlConfig(gameEngineConfig);
            gameEngineConfig.getPlanetConfig().setGameEngineMode(GameEngineMode.SLAVE);// TODO move to DB
            gameEngineService.fillSyncItems(gameEngineConfig.getPlanetConfig(), userContext);
            TemporaryPersistenceUtils.completePlanetConfigMultiPlayer(gameEngineConfig.getPlanetConfig());// TODO move to DB
        } else {
            // Tutorial
            gameUiControlConfig = getGameUiControlConfig4Level(1).toGameUiControlConfig(gameEngineConfig);
            gameEngineConfig.getPlanetConfig().setGameEngineMode(GameEngineMode.MASTER);// TODO move to DB
            TemporaryPersistenceUtils.completePlanetConfigTutorial(gameEngineConfig.getPlanetConfig());  // TODO move to DB
            gameUiControlConfig.setSceneConfigs(setupTutorial()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupMoveToMultiplayer()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupMove()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(findEnemyBase()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupAttack()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupTower()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupParticle()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupPickBox()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(setupThankYouForward()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(humanKillBotBase()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(killEnemyHarvester()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(killEnemyBotBase()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(killHumanBase()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(buildBase()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(harvest()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(useInventoryItem()); // TODO move to DB
            // gameUiControlConfig.setSceneConfigs(demolitionVisualization()); // TODO move to DB
        }
        gameUiControlConfig.setUserContext(userContext);
        gameUiControlConfig.setVisualConfig(defaultVisualConfig());  // TODO move to DB
        gameUiControlConfig.setAudioConfig(defaultAudioConfig());  // TODO move to DB
        gameUiControlConfig.setGameTipVisualConfig(defaultGameTipVisualConfig());  // TODO move to DB
        return gameUiControlConfig;
    }

    private GameUiControlConfigEntity getGameUiControlConfig4Level(long gameUiControlConfigEntityId) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameUiControlConfigEntity> query = criteriaBuilder.createQuery(GameUiControlConfigEntity.class);
        Root<GameUiControlConfigEntity> root = query.from(GameUiControlConfigEntity.class);
        query.where(criteriaBuilder.equal(root.get(GameUiControlConfigEntity_.id), gameUiControlConfigEntityId));
        CriteriaQuery<GameUiControlConfigEntity> userSelect = query.select(root);
        return entityManager.createQuery(userSelect).getSingleResult();
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
        gameTipVisualConfig.setSplashScrollImageId(272508);
        return gameTipVisualConfig;
    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(-27)).setShadowRotationY(Math.toRadians(0));
        visualConfig.setShape3DLightRotateX(Math.toRadians(60)).setShape3DLightRotateZ(Math.toRadians(260));
        visualConfig.setShape3Ds(shape3DPersistence.getShape3Ds());
        visualConfig.setBaseItemDemolitionImageId(180848);
        visualConfig.setBuildupTextureId(180818);
        visualConfig.setWaterConfig(defaultWaterConfig());
        return visualConfig;
    }

    private WaterConfig defaultWaterConfig() {
        WaterConfig waterConfig = new WaterConfig();
        waterConfig.setGroundLevel(-2).setBmDepth(7).setTransparency(0.5).setBmId(272480).setBmDepth(2).setBmScale(0.02);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(0.38, 0.38, 0.38));
        lightConfig.setRotationX(Math.toRadians(-33)).setRotationY(Math.toRadians(0)).setSpecularIntensity(0.75).setSpecularHardness(30);
        return waterConfig.setLightConfig(lightConfig);
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
        audioConfig.setOnOtherSelection(272528);
        audioConfig.setOnCommandSent(272529);
        audioConfig.setOnBaseLost(284040);
        return audioConfig;
    }

    private int getLevelNumber(int levelId) {
        for (LevelConfig levelConfig : gameEngineConfigPersistence.setupLevelConfigs()) {
            if (levelConfig.getLevelId() == levelId) {
                return levelConfig.getNumber();
            }
        }
        throw new IllegalArgumentException("No level for id: " + levelId);
    }

    // Move and tip  -----------------------------------------------------------------------------
    private List<SceneConfig> setupMove() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(104, 80));
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setInternalName("_setupMove").setViewFieldConfig(viewFieldConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(startConditionConfig).setXp(1).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quest geben Erfarungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten level. In der oberen linek Menu siehst du deine Erfahrungspubnkte.")).setRemoveLoadingCover(true));
        // Move quest
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(160, 70), new DecimalPosition(300, 70), new DecimalPosition(300, 200), new DecimalPosition(160, 200))))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.MOVE);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(200, 100));

        sceneConfigs.add(new SceneConfig().setInternalName("_setupMove").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Fahre zum Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setWait4LevelUpDialog(true));

        return sceneConfigs;
    }

    // Tower -----------------------------------------------------------------------------
    private List<SceneConfig> setupTower() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Tower bot
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_TOWER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(75, 246))).setNoSpawn(true).setNoRebuild(true));
        // botItems.add(new BotItemConfig().setBaseItemTypeId(BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(75, 246))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));

        sceneConfigs.add(new SceneConfig().setInternalName("_setupTower").setViewFieldConfig(viewFieldConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setBotConfigs(botConfigs).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Tower -----------------------------------------------------------------------------
    private List<SceneConfig> setupParticle() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        sceneConfigs.add(new SceneConfig().setInternalName("_setupParticle").setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(200, 200)).setCameraLocked(false)).setRemoveLoadingCover(true));
        return sceneConfigs;
    }

    // User InventoryItem -----------------------------------------------------------------------------
    private List<SceneConfig> useInventoryItem() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setInternalName("_useInventoryItem 1").setViewFieldConfig(viewFieldConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Start").setConditionConfig(startConditionConfig)).setWait4QuestPassedDialog(true).setRemoveLoadingCover(true));
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.INVENTORY_ITEM_PLACED).setComparisonConfig(new ComparisonConfig().setCount(1));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SPAN_INVENTORY_ITEM);
        gameTipConfig.setInventoryItemId(GameEngineConfigPersistence.INVENTORY_ITEM);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(50, 300));

        sceneConfigs.add(new SceneConfig().setInternalName("_useInventoryItem 2").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Build base -----------------------------------------------------------------------------
    private List<SceneConfig> buildBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        Map<Integer, Integer> startTypeCount = new HashMap<>();
        startTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig startConditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(startTypeCount));
        sceneConfigs.add(new SceneConfig().setInternalName("_buildBase 1").setViewFieldConfig(viewFieldConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setXp(100).setTitle("Platzieren").setDescription("Start").setConditionConfig(startConditionConfig)).setWait4QuestPassedDialog(true).setRemoveLoadingCover(true));
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig buildGameTipConfig = new GameTipConfig();
        buildGameTipConfig.setTip(GameTipConfig.Tip.BUILD);
        buildGameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        buildGameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        buildGameTipConfig.setTerrainPositionHint(new DecimalPosition(54, 260));
        sceneConfigs.add(new SceneConfig().setInternalName("_buildBase 2").setGameTipConfig(buildGameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Platziere deinen Bulldozer und baue eine Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        // Build Harvester Quest
        buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 1);
        conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig factoryGameTipConfig = new GameTipConfig();
        factoryGameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        factoryGameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        factoryGameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER);
        sceneConfigs.add(new SceneConfig().setInternalName("_buildBase 3").setGameTipConfig(factoryGameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue eine Harvester in deiner Fabrik").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Build base -----------------------------------------------------------------------------
    private List<SceneConfig> harvest() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setAllowedArea(new Rectangle2D(40, 210, 100, 100).toPolygon());
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(40, 170)).setCameraLocked(false);
        // Player base place
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(118, 262)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(121, 262)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(124, 262)).setRotationZ(Math.toRadians(160)));
        // Harvest quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(100));
        QuestConfig questConfig = new QuestConfig().setTitle("Sammle").setDescription("Sammle razarion um eine Armee zu bauen").setConditionConfig(conditionConfig);

        sceneConfigs.add(new SceneConfig().setInternalName("_harvest").setStartPointPlacerConfig(baseItemPlacerConfig).setViewFieldConfig(viewFieldConfig).setResourceItemTypePositions(resourceItemTypePositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true).setRemoveLoadingCover(true));
        return sceneConfigs;
    }

    // Kill human base -----------------------------------------------------------------------------
    private List<SceneConfig> killHumanBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(243, 120));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_killHumanBase 1").setViewFieldConfig(viewFieldConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setConditionConfig(conditionConfig).setTitle("Platzieren").setDescription("Platzieren")).setWait4QuestPassedDialog(true));
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Kill bot command
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        botKillHumanCommandConfigs.add(new BotKillHumanCommandConfig().setBotId(ENEMY_BOT).setDominanceFactor(2).setAttackerBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        // Camera
        sceneConfigs.add(new SceneConfig().setInternalName("_killHumanBase 2").setBotConfigs(botConfigs).setBotKillHumanCommandConfigs(botKillHumanCommandConfigs));
        return sceneConfigs;
    }

    // Kill enemy bot base -----------------------------------------------------------------------------
    private List<SceneConfig> killEnemyBotBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        addNpcBot(sceneConfigs);
        // Setup killer bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Kill bot command
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigss = new ArrayList<>();
        botKillOtherBotCommandConfigss.add(new BotKillOtherBotCommandConfig().setBotId(ENEMY_BOT).setTargetBotId(NPC_BOT_OUTPOST).setDominanceFactor(2).setAttackerBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(new Rectangle2D(250, 100, 50, 50).toPolygon())));
        // Camera
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_killEnemyBotBase").setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigss));
        return sceneConfigs;
    }

    // Human kill bot base -----------------------------------------------------------------------------
    private List<SceneConfig> humanKillBotBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Setup target bot
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Camera
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_humanKillBotBase 1").setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setRemoveLoadingCover(true));
        // User span
        addUserSpawnScene(sceneConfigs);
        // Kill bot base quest
        sceneConfigs.add(new SceneConfig().setInternalName("_humanKillBotBase 2").setQuestConfig(new QuestConfig().setTitle("Kill Bot").setTitle("Zerstöre den Bot").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BASE_KILLED).setComparisonConfig(new ComparisonConfig().setCount(1)))).setWait4QuestPassedDialog(true));
        // Go to than you page
        sceneConfigs.add(new SceneConfig().setInternalName("_humanKillBotBase 3").setForwardUrl("ThankYou.html"));
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
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(235, 170))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(250, 170))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(GameEngineConfigPersistence.RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(244, 187))).setHarvesterItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER));
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(GameEngineConfigPersistence.RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(264, 182))).setHarvesterItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER));
        // Camera
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(243, 80));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));

        Map<Integer, Integer> killItemTypeCount = new HashMap<>();
        killItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 1);
        sceneConfigs.add(new SceneConfig().setInternalName("_killEnemyHarvester 1").setQuestConfig(new QuestConfig().setConditionConfig(conditionConfig).setTitle("Platzieren").setDescription("Platzieren")).setWait4QuestPassedDialog(true).setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs).setResourceItemTypePositions(resourceItemTypePositions).setStartPointPlacerConfig(baseItemPlacerConfig));
        sceneConfigs.add(new SceneConfig().setInternalName("_killEnemyHarvester 2").setQuestConfig(new QuestConfig().setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(killItemTypeCount))).setTitle("Kill").setDescription("Kill 2")).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Find Pick Box -----------------------------------------------------------------------------
    private List<SceneConfig> setupPickBox() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(GameEngineConfigPersistence.BOX_ITEM_TYPE).setPosition(new DecimalPosition(110, 80)));
        // Camera
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);

        sceneConfigs.add(new SceneConfig().setInternalName("_setupPickBox 1").setViewFieldConfig(viewFieldConfig).setBoxItemPositions(boxItemPositions).setRemoveLoadingCover(true));
        addUserSpawnScene(sceneConfigs);

        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1))).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden.");
        sceneConfigs.add(new SceneConfig().setInternalName("_setupPickBox 2").setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
        sceneConfigs.add(new SceneConfig().setInternalName("_setupPickBox 3").setForwardUrl("ThankYou.html"));
        return sceneConfigs;
    }

    // Thank you forward  -----------------------------------------------------------------------------
    private List<SceneConfig> setupThankYouForward() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();

        // Camera
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);

        sceneConfigs.add(new SceneConfig().setInternalName("_setupThankYouForward 1").setViewFieldConfig(viewFieldConfig).setRemoveLoadingCover(true));
        addUserSpawnScene(sceneConfigs);

        sceneConfigs.add(new SceneConfig().setInternalName("_setupThankYouForward 2").setForwardUrl("ThankYou.html"));
        return sceneConfigs;
    }

    // Find Enemy Base -----------------------------------------------------------------------------
    private List<SceneConfig> findEnemyBase() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Bot Attacker
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> attackerEnragement = new ArrayList<>();
        List<BotItemConfig> attackerBotItems = new ArrayList<>();
        attackerBotItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(305, 175))).setNoSpawn(true));
        attackerEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerBotItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragement).setName("Kenny").setNpc(false));
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(300, 170, 10, 10)).setXp(1).setPassedMessage("Gratuliere, du hast die gegnerische Basis gefunden");
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SCROLL);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(305, 175));
        // div
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_findEnemyBase").setGameTipConfig(gameTipConfig).setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Attack -----------------------------------------------------------------------------
    private List<SceneConfig> setupAttack() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        List<BotConfig> botConfigs = new ArrayList<>();
        // Bot Target
        List<BotEnragementStateConfig> targetEnragement = new ArrayList<>();
        List<BotItemConfig> targetBotItems = new ArrayList<>();
        targetBotItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(100, 80))).setNoSpawn(true).setNoRebuild(true));
        targetEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(targetBotItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_INSTRUCTOR).setActionDelay(3000).setBotEnragementStateConfigs(targetEnragement).setName("Kenny").setNpc(true));
        // Bot Attacker
        List<BotEnragementStateConfig> attackerEnragement = new ArrayList<>();
        List<BotItemConfig> attackerBotItems = new ArrayList<>();
        attackerBotItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(90, 80))).setNoSpawn(true).setNoRebuild(true));
        attackerEnragement.add(new BotEnragementStateConfig().setName("Normal").setBotItems(attackerBotItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(attackerEnragement).setName("Kenny").setNpc(false));
        // Attack command
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(100, 80))).setActorItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER));
        // div
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(104, 32)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_setupAttack").setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setBotAttackCommandConfigs(botAttackCommandConfigs).setRemoveLoadingCover(true));
        return sceneConfigs;
    }

    // Demolition Visualization -----------------------------------------------------------------------------
    private List<SceneConfig> demolitionVisualization() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(270, 260)).setCameraLocked(false);
        List<BotConfig> botConfigs = new ArrayList<>();
        // Bot target
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(270, 260))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(false));
        // Bot attacker
        botEnragementStateConfigs = new ArrayList<>();
        botItems = new ArrayList<>();
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(220, 260))).setNoSpawn(true).setNoRebuild(true));
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Bobby").setNpc(true));
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs = new ArrayList<>();
        botKillOtherBotCommandConfigs.add(new BotKillOtherBotCommandConfig().setBotId(NPC_BOT_OUTPOST).setTargetBotId(ENEMY_BOT).setDominanceFactor(1).setAttackerBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(213, 220, 50, 50))));

        sceneConfigs.add(new SceneConfig().setInternalName("_demolitionVisualization").setRemoveLoadingCover(true).setViewFieldConfig(viewFieldConfig).setBotConfigs(botConfigs).setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigs).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Demolition Move to Multiplayer planet -----------------------------------------------------------------------------
    private List<SceneConfig> setupMoveToMultiplayer() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(243, 120));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(243, 90)).setCameraLocked(false);
        sceneConfigs.add(new SceneConfig().setInternalName("_killHumanBase 1").setRemoveLoadingCover(true).setViewFieldConfig(viewFieldConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setXp(10).setConditionConfig(conditionConfig).setTitle("Platzieren").setDescription("Platzieren")).setWait4QuestPassedDialog(true));
        return sceneConfigs;
    }

    // Tutorial -----------------------------------------------------------------------------
    private List<SceneConfig> setupTutorial() {
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        // Level 1
        addResources(sceneConfigs);
        addNpcBot(sceneConfigs);
        addEnemyBot(sceneConfigs);
        addFadeOutLoadingCover(sceneConfigs);
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
        addWaitForDeadTask(sceneConfigs);
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
        addKillFactory(sceneConfigs);
        return sceneConfigs;
    }

    private void addResources(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName("setup: add resources");
        List<ResourceItemPosition> resourceItemTypePositions = new ArrayList<>();
        // Outpost
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(212, 144)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(233, 164)).setRotationZ(Math.toRadians(80)));
        // Outpost 2
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(96, 254)).setRotationZ(Math.toRadians(0)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(108, 254)).setRotationZ(Math.toRadians(80)));
        resourceItemTypePositions.add(new ResourceItemPosition().setId(1).setResourceItemTypeId(180829).setPosition(new DecimalPosition(120, 252)).setRotationZ(Math.toRadians(160)));

        sceneConfig.setResourceItemTypePositions(resourceItemTypePositions);
        sceneConfigs.add(sceneConfig);
    }

    private void addNpcBot(List<SceneConfig> sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(223, 130))).setAngle(Math.toRadians(110)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(220, 109))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(213, 92))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(207, 111))).setAngle(Math.toRadians(30)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 94))).setAngle(Math.toRadians(175)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(201, 88))).setAngle(Math.toRadians(310)).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(NPC_BOT_OUTPOST).setResourceItemTypeId(GameEngineConfigPersistence.RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(212, 144))).setHarvesterItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER));
        sceneConfigs.add(new SceneConfig().setInternalName("setup: add NPC bot").setBotConfigs(botConfigs).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
    }

    private void addEnemyBot(List<SceneConfig> sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_TOWER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(190, 242))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(248, 283))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 296))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(299, 261))).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(240, 255))).setAngle(Math.toRadians(100)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(277, 252))).setAngle(Math.toRadians(200)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(260, 227))).setAngle(Math.toRadians(333)).setNoSpawn(true).setNoRebuild(true));
        // Attackers 4 harvester
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(230, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(234, 187))).setAngle(Math.toRadians(260)).setNoSpawn(true).setNoRebuild(true));
        // Harvester to harvest after attack
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(253, 200))).setAngle(Math.toRadians(240)).setNoSpawn(true).setNoRebuild(true));

        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(ENEMY_BOT).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Razar Industries").setNpc(false));
        sceneConfigs.add(new SceneConfig().setInternalName("setup: add enemy bot").setBotConfigs(botConfigs).setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(270, 275)).setCameraLocked(true).setBottomWidth(120.0)));
    }

    private void addFadeOutLoadingCover(List<SceneConfig> sceneConfigs) {
        sceneConfigs.add(new SceneConfig().setInternalName("script: fade out").setRemoveLoadingCover(true));
    }

    private void addScrollOverTerrain(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName("script: scroll over terrain").setIntroText("Willkommen Kommandant, Razarion Industries betreibt Raubbau auf diesem Planeten. Ihre Aufgabe ist es, Razarion Industries von diesem Planeten zu vertreiben.");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setFromPosition(new DecimalPosition(270, 275)).setToPosition(new DecimalPosition(116, 84)).setSpeed(50.0).setCameraLocked(true).setBottomWidth(120.0));
        sceneConfigs.add(sceneConfig);
    }

    private void addBotSpawnScene(List<SceneConfig> sceneConfigs) {
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(116, 100))).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_INSTRUCTOR).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
        sceneConfigs.add(new SceneConfig().setInternalName("script: npc bot spawn").setBotConfigs(botConfigs).setIntroText("Kenny unterstützt Dich dabei. Er wird sich gleich auf die Planetenoberfläche beamen.").setDuration(3000));
    }

    private void addUserSpawnScene(List<SceneConfig> sceneConfigs) {
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setSuggestedPosition(new DecimalPosition(135, 85));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(135, 85));

        sceneConfigs.add(new SceneConfig().setInternalName("user: spawn 1").setGameTipConfig(gameTipConfig).setWait4QuestPassedDialog(true).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, du hast soeben deinen ersten Quest bestanden. Quests geben Erfahrungspunkte (Ep). Hast du genügend Erfahrungspunkte, erreichst du den nächsten Level. Im oberen linken Bereich siehst du deine Erfahrungspunkte.")));
    }

    private void addBotMoveScene(List<SceneConfig> sceneConfigs) {
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setToPosition(new DecimalPosition(205, 102)).setSpeed(50.0).setCameraLocked(true);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(NPC_BOT_INSTRUCTOR).setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setTargetPosition(new DecimalPosition(188, 90)));
        sceneConfigs.add(new SceneConfig().setInternalName("script: npc bot move").setViewFieldConfig(viewFieldConfig).setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Folge mir zum Vorposten"));
    }

    private void addScrollToOwnScene(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName("script: scroll to user").setIntroText("Fahre deine Einheit zum Vorposten");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(160, 100)).setSpeed(50.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void addUserMoveScene(List<SceneConfig> sceneConfigs) {
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(175, 103, 10, 10))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.MOVE);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(180, 108));

        sceneConfigs.add(new SceneConfig().setInternalName("user: move").setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true));
    }

    private void addNpcHarvestAttack(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setInternalName("script: bot harvest attack");
        sceneConfig.setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(212, 144)).setSpeed(50.0).setCameraLocked(true));
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setActorItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(190, 124, 40, 40))));
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(ENEMY_BOT).setTargetItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER).setActorItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setTargetSelection(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(190, 124, 40, 40))));
        sceneConfig.setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(7000).setIntroText("Hilfe wir werden angegriffen");
        sceneConfigs.add(sceneConfig);
    }

    private void addFindEnemyBase(List<SceneConfig> sceneConfigs) {
        // Scroll Quest
        ScrollUiQuest scrollUiQuest = new ScrollUiQuest().setXp(1).setTitle("Finde Gegenerbasis").setDescription("Scrolle und such die gegenrische Basis").setScrollTargetRectangle(new Rectangle2D(250, 290, 10, 10)).setXp(1).setPassedMessage("Gratuliere, du hast die gegnerische Basis gefunden");
        // div
        ViewFieldConfig viewFieldConfig = new ViewFieldConfig().setCameraLocked(false);
        List<BotHarvestCommandConfig> botHarvestCommandConfigs = new ArrayList<>();
        botHarvestCommandConfigs.add(new BotHarvestCommandConfig().setBotId(ENEMY_BOT).setResourceItemTypeId(GameEngineConfigPersistence.RESOURCE_ITEM_TYPE).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(212, 144))).setHarvesterItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SCROLL);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(270, 310));

        sceneConfigs.add(new SceneConfig().setInternalName("user: find enemy base").setGameTipConfig(gameTipConfig).setViewFieldConfig(viewFieldConfig).setScrollUiQuest(scrollUiQuest).setWait4QuestPassedDialog(true).setBotHarvestCommandConfigs(botHarvestCommandConfigs));
    }

    private void addPickBoxTask(List<SceneConfig> sceneConfigs) {
        // Drop box
        List<BoxItemPosition> boxItemPositions = new ArrayList<>();
        boxItemPositions.add(new BoxItemPosition().setBoxItemTypeId(GameEngineConfigPersistence.BOX_ITEM_TYPE).setPosition(new DecimalPosition(188, 116)));
        // Pick box quest
        QuestConfig questConfig = new QuestConfig().setXp(1).setTitle("Nimm die Box").setDescription("Eine Box wurde gesichtet. Sammle sie auf").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.BOX_PICKED).setComparisonConfig(new ComparisonConfig().setCount(1)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.PICK_BOX);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setToGrabItemTypeId(GameEngineConfigPersistence.BOX_ITEM_TYPE);

        sceneConfigs.add(new SceneConfig().setInternalName("user: pick box").setGameTipConfig(gameTipConfig).setBoxItemPositions(boxItemPositions).setQuestConfig(questConfig).setWait4QuestPassedDialog(true));
    }

    private void addBoxSpawnTask(List<SceneConfig> sceneConfigs) {
        // Use inventory item quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setCount(1));
        // Move attackers away
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(ENEMY_BOT).setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setTargetPosition(new DecimalPosition(255, 244)));
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(ENEMY_BOT).setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setTargetPosition(new DecimalPosition(257, 246)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.SPAN_INVENTORY_ITEM);
        gameTipConfig.setInventoryItemId(GameEngineConfigPersistence.INVENTORY_ITEM);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(216, 125));
        sceneConfigs.add(new SceneConfig().setInternalName("user: box spawn").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setXp(1).setTitle("Benutze Inventar").setDescription("Platziere die Militäreinheiten vom Inventar").setConditionConfig(conditionConfig)).setWait4QuestPassedDialog(true).setBotMoveCommandConfigs(botMoveCommandConfigs));
    }

    private void addAttackTask(List<SceneConfig> sceneConfigs) {
        // Attack quest
        Map<Integer, Integer> attackItemTypeCount = new HashMap<>();
        attackItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 1);
        QuestConfig questConfig = new QuestConfig().setXp(10).setTitle("Zerstöre die Abbaufahrzeuge").setDescription("Greiffe Razarion insudtries an und zerstöre die Abbaufahrzeuge").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(attackItemTypeCount)));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPolygon2D(new Rectangle2D(194, 133, 50, 50).toPolygon()));

        sceneConfigs.add(new SceneConfig().setInternalName("user: kill bot harvester").setQuestConfig(questConfig).setGameTipConfig(gameTipConfig).setWait4LevelUpDialog(true));
    }

    private void addEnemyKillTask(List<SceneConfig> sceneConfigs) {
        // Kill bot command
        List<BotKillOtherBotCommandConfig> botKillOtherBotCommandConfigs = new ArrayList<>();
        botKillOtherBotCommandConfigs.add(new BotKillOtherBotCommandConfig().setBotId(ENEMY_BOT).setTargetBotId(NPC_BOT_OUTPOST).setDominanceFactor(1).setAttackerBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(212, 162, 51, 87))));
        // Kill human command
        List<BotKillHumanCommandConfig> botKillHumanCommandConfigs = new ArrayList<>();
        botKillHumanCommandConfigs.add(new BotKillHumanCommandConfig().setBotId(ENEMY_BOT).setDominanceFactor(2).setAttackerBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setSpawnPoint(new PlaceConfig().setPolygon2D(Polygon2D.fromRectangle(213, 209, 80, 70))));
        sceneConfigs.add(new SceneConfig().setInternalName("script: enemy bot destroy user").setBotKillHumanCommandConfigs(botKillHumanCommandConfigs).setBotKillOtherBotCommandConfigs(botKillOtherBotCommandConfigs).setIntroText("Hilfe, Razar Industries greift uns an").setDuration(4000));
    }

    private void addWaitForDeadTask(List<SceneConfig> sceneConfigs) {
        sceneConfigs.add(new SceneConfig().setInternalName("script wait for dead dialog").setWaitForBaseLostDialog(true));
    }

    private void addNpcEscapeTask(List<SceneConfig> sceneConfigs) {
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(NPC_BOT_INSTRUCTOR).setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER).setTargetPosition(new DecimalPosition(48, 110)));
        sceneConfigs.add(new SceneConfig().setInternalName("script: escape npc bot").setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Baue dich neu auf und zerstöre Razar Industries.").setDuration(3000));
    }

    private void addUserSpawnScene2(List<SceneConfig> sceneConfigs) {
        // Bot NPC_BOT_OUTPOST_2
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(145, 260))).setNoSpawn(true).setNoRebuild(true));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(NPC_BOT_OUTPOST_2).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Roger").setNpc(true));
        // User Spawn
        BaseItemPlacerConfig baseItemPlacerConfig = new BaseItemPlacerConfig().setEnemyFreeRadius(10).setAllowedArea(Polygon2D.fromRectangle(80, 260, 50, 50));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.START_PLACER);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(100, 280));

        // Kill NPC_BOT_INSTRUCTOR
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotId(NPC_BOT_INSTRUCTOR));
        // Build factory Quest
        sceneConfigs.add(new SceneConfig().setInternalName("user: spawn 2").setGameTipConfig(gameTipConfig).setStartPointPlacerConfig(baseItemPlacerConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Basis").setDescription("Platziere deinen Bulldozer und baue eine Basis auf um Razarion Industries zu besiegen.").setWaitButHidePassedDialog(true).setConditionConfig(conditionConfig).setXp(0)).setKillBotCommandConfigs(killBotCommandConfigs).setBotConfigs(botConfigs));
    }

    private void addBuildFactoryTask(List<SceneConfig> sceneConfigs) {
        // Build factory Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.BUILD);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_BULLDOZER);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setTerrainPositionHint(new DecimalPosition(112, 285));

        sceneConfigs.add(new SceneConfig().setInternalName("user: build factory").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue eine Fabrik").setDescription("Baue eine Fabrik mit deinem Bulldozer").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addFactorizeHarvesterTask(List<SceneConfig> sceneConfigs) {
        // Build Harvester Quest
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Kill NPC_BOT_OUTPOST
        List<KillBotCommandConfig> killBotCommandConfigs = new ArrayList<>();
        killBotCommandConfigs.add(new KillBotCommandConfig().setBotId(NPC_BOT_OUTPOST));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER);

        sceneConfigs.add(new SceneConfig().setInternalName("user: fabricate harvester").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Baue ein Harvester").setDescription("Baue ein Harvester in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true).setKillBotCommandConfigs(killBotCommandConfigs));
    }

    private void addHarvestTask(List<SceneConfig> sceneConfigs) {
        // Harvest quest
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.HARVEST).setComparisonConfig(new ComparisonConfig().setCount(30));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.HARVEST);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_HARVESTER);
        gameTipConfig.setToGrabItemTypeId(GameEngineConfigPersistence.RESOURCE_ITEM_TYPE);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(108, 254)));
        sceneConfigs.add(new SceneConfig().setInternalName("user: harvest").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Sammle Razarion").setDescription("Sammle Razarion um eine Armee zu bauen").setConditionConfig(conditionConfig).setXp(10)).setWait4LevelUpDialog(true));
    }

    private void addHarvestExplanationTask(List<SceneConfig> sceneConfigs) {
        // Harvest explanation
        sceneConfigs.add(new SceneConfig().setInternalName("script: explain harvest").setIntroText("Du brauchst viel Razarion um eine Armee zu bauen").setDuration(3000));
    }

    private void addBuildViperTask(List<SceneConfig> sceneConfigs) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER);

        sceneConfigs.add(new SceneConfig().setInternalName("user: build viper 1").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue ein Viper in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addNpcAttackTowerCommand(List<SceneConfig> sceneConfigs) {
        // Attack command
        List<BotAttackCommandConfig> botAttackCommandConfigs = new ArrayList<>();
        botAttackCommandConfigs.add(new BotAttackCommandConfig().setBotId(NPC_BOT_OUTPOST_2).setTargetItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_TOWER).setTargetSelection(new PlaceConfig().setPosition(new DecimalPosition(190, 242))).setActorItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER));
        sceneConfigs.add(new SceneConfig().setInternalName("script: npc bot attacks tower").setIntroText("Komm, greiffen wir an!").setBotAttackCommandConfigs(botAttackCommandConfigs).setDuration(5000).setViewFieldConfig(new ViewFieldConfig().setToPosition(new DecimalPosition(190, 242)).setSpeed(50.0)));
    }

    private void addNpcTooWeakCommand(List<SceneConfig> sceneConfigs) {
        // Attack command
        sceneConfigs.add(new SceneConfig().setInternalName("script: npc too weak").setIntroText("Der Turm ist zu stark, wir brauchen eine grössere Armee").setDuration(2000));
    }

    private void addBuildViperTask2(List<SceneConfig> sceneConfigs) {
        // Build viper
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER, 2);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.FABRICATE);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_FACTORY);
        gameTipConfig.setToCreatedItemTypeId(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER);

        sceneConfigs.add(new SceneConfig().setInternalName("user: build viper 2").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Bauen").setDescription("Baue zwei Vipers in deiner Fabrik").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addKillTower(List<SceneConfig> sceneConfigs) {
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(GameEngineConfigPersistence.BASE_ITEM_TYPE_TOWER, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setTypeCount(buildupItemTypeCount));
        // Tip
        GameTipConfig gameTipConfig = new GameTipConfig();
        gameTipConfig.setTip(GameTipConfig.Tip.ATTACK);
        gameTipConfig.setActor(GameEngineConfigPersistence.BASE_ITEM_TYPE_ATTACKER);
        gameTipConfig.setPlaceConfig(new PlaceConfig().setPosition(new DecimalPosition(190, 242)));

        sceneConfigs.add(new SceneConfig().setInternalName("user: kill tower").setGameTipConfig(gameTipConfig).setQuestConfig(new QuestConfig().setTitle("Zerstöre Turm").setDescription("Nimm deine 3 Vipers und zerstöre den Turm").setConditionConfig(conditionConfig).setXp(10)).setWait4QuestPassedDialog(true));
    }

    private void addKillFactory(List<SceneConfig> sceneConfigs) {
        // Kill bot base quest
        sceneConfigs.add(new SceneConfig().setInternalName("user: kill factory").setQuestConfig(new QuestConfig().setXp(20).setTitle("Zerstöre Fabriken").setDescription("Zerstöre die Fabriken, damit Razar Industries auf diesem Planeten nicht weiter existieren kann.").setConditionConfig(new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_KILLED).setComparisonConfig(new ComparisonConfig().setCount(2)))).setWait4QuestPassedDialog(true));
        // Go to than you page
        // sceneConfigs.add(new SceneConfig().setInternalName("script: forward to ThankYou page").setForwardUrl("ThankYou.html"));
    }
}
