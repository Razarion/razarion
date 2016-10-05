package com.btxtech.server.persistence.impl;

import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.server.persistence.StoryboardEntity;
import com.btxtech.server.persistence.TerrainElementPersistence;
import com.btxtech.server.persistence.itemtype.ItemTypePersistence;
import com.btxtech.servercommon.StoryboardPersistence;
import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.CameraConfig;
import com.btxtech.shared.dto.LightConfig;
import com.btxtech.shared.dto.SceneConfig;
import com.btxtech.shared.dto.StartPointConfig;
import com.btxtech.shared.dto.StoryboardConfig;
import com.btxtech.shared.dto.VisualConfig;
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
    @PersistenceContext
    private EntityManager entityManager;
    @Inject
    private TerrainElementPersistence terrainElementPersistence;
    @Inject
    private Shape3DPersistence shape3DPersistence;
    @Inject
    private ItemTypePersistence itemTypePersistence;

    @Override
    @Transactional
    public StoryboardConfig load() throws ParserConfigurationException, SAXException, IOException {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setSlopeSkeletonConfigs(terrainElementPersistence.loadSlopeSkeletons());
        gameEngineConfig.setGroundSkeletonConfig(terrainElementPersistence.loadGroundSkeleton());
        gameEngineConfig.setTerrainObjectConfigs(terrainElementPersistence.readTerrainObjects());
        gameEngineConfig.setBaseItemTypes(itemTypePersistence.readBaseItemType());
        gameEngineConfig.setResourceItemTypes(itemTypePersistence.readResourceItemType());
        gameEngineConfig.setLevelConfigs(setupLevelConfigs());  // TODO mode to DB
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // Query for total row count in invitations
        CriteriaQuery<StoryboardEntity> userQuery = criteriaBuilder.createQuery(StoryboardEntity.class);
        Root<StoryboardEntity> from = userQuery.from(StoryboardEntity.class);
        CriteriaQuery<StoryboardEntity> userSelect = userQuery.select(from);
        StoryboardConfig storyboardConfig = entityManager.createQuery(userSelect).getSingleResult().toStoryboardConfig(gameEngineConfig);
        storyboardConfig.setUserContext(new UserContext().setName("Emulator Name").setLevelId(1));  // TODO mode to DB
        storyboardConfig.setVisualConfig(defaultVisualConfig());  // TODO mode to DB
        List<SceneConfig> sceneConfigs = new ArrayList<>();
        addSetupItems(sceneConfigs); // TODO mode to DB
        // addScrollOverTerrain(sceneConfigs); // TODO mode to DB
        //addBotSpawnScene(sceneConfigs); // TODO mode to DB
        // addUserSpawnScene(sceneConfigs); // TODO mode to DB
        //addBotMoveScene(sceneConfigs);// TODO mode to DB
        //addScrollToOwnScene(sceneConfigs);// TODO mode to DB
        // addUserMoveScene(sceneConfigs);// TODO mode to DB
        completePlanetConfig(gameEngineConfig.getPlanetConfig());  // TODO mode to DB
        storyboardConfig.setSceneConfigs(sceneConfigs);
        return storyboardConfig;
    }

    private VisualConfig defaultVisualConfig() throws IOException, SAXException, ParserConfigurationException {
        // TODO remove this method. Make method which creates a new default Storyboard
        VisualConfig visualConfig = new VisualConfig();
        visualConfig.setShadowAlpha(0.2).setShadowRotationX(Math.toRadians(25)).setShadowRotationZ(Math.toRadians(250));
        visualConfig.setShape3DLightRotateX(Math.toRadians(25)).setShape3DLightRotateZ(Math.toRadians(290));
        visualConfig.setWaterGroundLevel(-20).setWaterBmDepth(10).setWaterTransparency(0.65);
        LightConfig lightConfig = new LightConfig();
        lightConfig.setDiffuse(new Color(1, 1, 1)).setAmbient(new Color(1, 1, 1)).setRotationX(Math.toRadians(-20));
        lightConfig.setRotationY(Math.toRadians(-20)).setSpecularIntensity(1.0).setSpecularHardness(0.5);
        visualConfig.setWaterLightConfig(lightConfig);
        visualConfig.setShape3DGeneralScale(10).setShape3Ds(shape3DPersistence.getShape3Ds());
        return visualConfig;
    }

    private void addSetupItems(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig();
        sceneConfig.setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(1040, 320)).setCameraLocked(false));
        Map<Integer, DecimalPosition> resourceItemTypePositions = new HashMap<>();
        resourceItemTypePositions.put(180829, new DecimalPosition(1040, 800));
        sceneConfig.setResourceItemTypePositions(resourceItemTypePositions);
        sceneConfigs.add(sceneConfig);
    }

    private void addScrollOverTerrain(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setIntroText("Willkommen Kommandant, Razarion Industries betreibt Raubbau auf diesem Planeten. Ihre Aufgabe ist es, Razarion Industries von diesem Planeten zu vertreiben.");
        sceneConfig.setCameraConfig(new CameraConfig().setFromPosition(new DecimalPosition(3260, 2900)).setToPosition(new DecimalPosition(1040, 320)).setSpeed(1000.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void addBotSpawnScene(List<SceneConfig> sceneConfigs) {
        // List<SceneConfig> sceneConfigs = new ArrayList<>();
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(1040, 320)).setCameraLocked(true);
        List<BotConfig> botConfigs = new ArrayList<>();
        List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
        List<BotItemConfig> botItems = new ArrayList<>();
        botItems.add(new BotItemConfig().setBaseItemTypeId(180807).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(1040, 800))));
        botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
        botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotConfigs(botConfigs).setIntroText("Kenny unterstützt Dich dabei. Er wird sich gleich auf die Planetenoberfläche beamen."));
    }

    private void addUserSpawnScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(1040, 320)).setCameraLocked(true);
        StartPointConfig startPointConfig = new StartPointConfig().setBaseItemTypeId(180807).setEnemyFreeRadius(100).setSuggestedPosition(new DecimalPosition(1040, 800));
        Map<Integer, Integer> buildupItemTypeCount = new HashMap<>();
        buildupItemTypeCount.put(180807, 1);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_CREATED).setComparisonConfig(new ComparisonConfig().setBaseItemTypeCount(buildupItemTypeCount));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setStartPointConfig(startPointConfig).setQuestConfig(new QuestConfig().setTitle("Platzieren").setDescription("Wähle deinen Startpunkt um deine Starteinheit zu platzieren").setConditionConfig(conditionConfig).setXp(1).setPassedMessage("Gratuliere, Du hast soeben deinen ersten Quest bestanden")));
    }

    private void addBotMoveScene(List<SceneConfig> sceneConfigs) {
        CameraConfig cameraConfig = new CameraConfig().setToPosition(new DecimalPosition(2040, 520)).setSpeed(80.0).setCameraLocked(false);
        List<BotMoveCommandConfig> botMoveCommandConfigs = new ArrayList<>();
        botMoveCommandConfigs.add(new BotMoveCommandConfig().setBotId(1).setBaseItemTypeId(180807).setDecimalPosition(new DecimalPosition(2040, 1000)));
        sceneConfigs.add(new SceneConfig().setCameraConfig(cameraConfig).setBotMoveCommandConfigs(botMoveCommandConfigs).setIntroText("Folge mir zum Vorposten"));
    }

    private void addScrollToOwnScene(List<SceneConfig> sceneConfigs) {
        SceneConfig sceneConfig = new SceneConfig().setIntroText("Fahre deine Einheit zum Vorposten");
        sceneConfig.setCameraConfig(new CameraConfig().setToPosition(new DecimalPosition(1640, 320)).setSpeed(500.0).setCameraLocked(true));
        sceneConfigs.add(sceneConfig);
    }

    private void addUserMoveScene(List<SceneConfig> sceneConfigs) {
        Map<Integer, Integer> itemTypeCount = new HashMap<>();
        itemTypeCount.put(180807, 1);
        ComparisonConfig comparisonConfig = new ComparisonConfig().setBaseItemTypeCount(itemTypeCount).setPlaceConfig(new PlaceConfig().setPolygon2D(new Polygon2D(Arrays.asList(new DecimalPosition(1600, 700), new DecimalPosition(2000, 700), new DecimalPosition(2000, 1000), new DecimalPosition(1600, 1000))))).setAddExisting(true);
        ConditionConfig conditionConfig = new ConditionConfig().setConditionTrigger(ConditionTrigger.SYNC_ITEM_POSITION).setComparisonConfig(comparisonConfig);
        sceneConfigs.add(new SceneConfig().setCameraConfig(new CameraConfig().setCameraLocked(false)).setQuestConfig(new QuestConfig().setTitle("Fahre zu Vorposten").setDescription("Folge Kenny und Fahre zum Vorposten. Bewege Deine Einheit zum markierten Bereich").setXp(1).setConditionConfig(conditionConfig)).setWait4LevelUp(true));
    }

    private List<LevelConfig> setupLevelConfigs() {
        List<LevelConfig> levelConfigs = new ArrayList<>();
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(180807, 1);
        levelConfigs.add(new LevelConfig().setLevelId(1).setNumber(1).setXp2LevelUp(2).setItemTypeLimitation(itemTypeLimitation));
        levelConfigs.add(new LevelConfig().setLevelId(2).setNumber(2).setXp2LevelUp(10).setItemTypeLimitation(itemTypeLimitation));
        return levelConfigs;
    }

    private void completePlanetConfig(PlanetConfig planetConfig) {
        planetConfig.setHouseSpace(10);
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(180807, 1);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        planetConfig.setGroundMeshDimension(new Rectangle(0, 0, 64, 64));
    }

}
