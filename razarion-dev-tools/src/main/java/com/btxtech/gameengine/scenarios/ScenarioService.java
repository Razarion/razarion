package com.btxtech.gameengine.scenarios;

import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.datatypes.InventoryItem;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.QuestConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.BoxItemTypePossibility;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.FactoryType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.WeaponType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.BoxService;
import com.btxtech.shared.gameengine.planet.CommandService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.shared.gameengine.planet.quest.QuestListener;
import com.btxtech.shared.gameengine.planet.quest.QuestService;
import com.btxtech.shared.utils.CollectionUtils;
import com.btxtech.webglemulator.razarion.DevToolsSimpleExecutorServiceImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ScenarioService implements QuestListener {
    static final BaseItemType SIMPLE_MOVABLE_ITEM_TYPE;
    static final BaseItemType SIMPLE_FIX_ITEM_TYPE;
    static final BaseItemType HARVESTER_ITEM_TYPE;
    static final BaseItemType ATTACKER_ITEM_TYPE;
    static final BaseItemType BUILDER_ITEM_TYPE;
    static final BaseItemType FACTORY_ITEM_TYPE;
    static final ResourceItemType RESOURCE_ITEM_TYPE;
    static final BoxItemType BOX_ITEM_TYPE;
    static final InventoryItem INVENTORY_ITEM;
    static final int LEVEL_1_ID = 1;
    static final int SLOPE_ID = 1;
    static final int TERRAIN_OBJECT_ID = 1;
    @Inject
    private GameEngine gameEngine;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private DevToolsSimpleExecutorServiceImpl devToolsSimpleExecutorService;
    @Inject
    private BotService botService;
    @Inject
    private ResourceService resourceService;
    @Inject
    private BoxService boxService;
    @Inject
    private CommandService commandService;
    @Inject
    private QuestService questService;
    private List<ScenarioSuite> scenarioSuites = new ArrayList<>();
    private Scenario currentScenario;

    static {
        int itemId = 0;
        BaseItemType simpleMovable = new BaseItemType();
        simpleMovable.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setName("Simple Movable");
        simpleMovable.setId(++itemId);
        simpleMovable.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        SIMPLE_MOVABLE_ITEM_TYPE = simpleMovable;

        BaseItemType simpleFix = new BaseItemType();
        simpleFix.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setName("Simple Fix");
        simpleFix.setId(++itemId);
        simpleFix.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(2));
        SIMPLE_FIX_ITEM_TYPE = simpleFix;

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(100).setSpawnDurationMillis(1000).setBuildup(10).setName("Harvester");
        harvester.setId(++itemId);
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        HARVESTER_ITEM_TYPE = harvester;

        BaseItemType attacker = new BaseItemType();
        attacker.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(5).setBuildup(10).setName("Attacker");

        attacker.setId(++itemId);
        attacker.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        attacker.setWeaponType(new WeaponType().setMuzzlePosition(new Vertex(2, 0, 1)).setProjectileSpeed(17.0).setRange(20).setReloadTime(0.3).setDamage(1));
        ATTACKER_ITEM_TYPE = attacker;

        BaseItemType factory = new BaseItemType();
        factory.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(5).setBuildup(3).setName("Factory");

        factory.setId(++itemId);
        factory.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(5));
        FACTORY_ITEM_TYPE = factory;

        BaseItemType builder = new BaseItemType();
        builder.setHealth(100).setSpawnDurationMillis(1000).setBoxPickupRange(5).setBuildup(10).setName("Builder");
        builder.setId(++itemId);
        builder.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        builder.setBuilderType(new BuilderType().setProgress(1).setRange(3).setAbleToBuild(Collections.singletonList(FACTORY_ITEM_TYPE.getId())));
        BUILDER_ITEM_TYPE = builder;

        // Finalize factory
        factory.setFactoryType(new FactoryType().setProgress(1.0).setAbleToBuildId(Arrays.asList(BUILDER_ITEM_TYPE.getId(), HARVESTER_ITEM_TYPE.getId())));

        ResourceItemType resource = new ResourceItemType();
        resource.setRadius(2).setAmount(1000).setId(++itemId);
        RESOURCE_ITEM_TYPE = resource;

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setId(++itemId).setName("Inventory Item Name").setBaseItemType(ATTACKER_ITEM_TYPE.getId()).setBaseItemTypeCount(1);
        INVENTORY_ITEM = inventoryItem;

        BoxItemType box = new BoxItemType();
        box.setRadius(0.5);
        box.setId(++itemId);
        box.setBoxItemTypePossibilities(Collections.singletonList(new BoxItemTypePossibility().setPossibility(1.0).setInventoryItemId(INVENTORY_ITEM.getId())));
        BOX_ITEM_TYPE = box;
    }

    @PostConstruct
    public void postConstruct() {
        questService.addQuestListener(this);
        setupScenarioSuits();
        currentScenario = findStartScenario();
    }

    private void addScenarioSuite(ScenarioSuite scenarioSuite) {
        scenarioSuites.add(scenarioSuite);
    }

    public void startNextScenario() {
        Scenario newScenario = getScenarioSuite(currentScenario).getNext(currentScenario);
        if (newScenario == null) {
            ScenarioSuite scenarioSuite = CollectionUtils.safeListAccess(scenarioSuites, getScenarioSuitIndex(currentScenario) + 1);
            newScenario = scenarioSuite.getFirst();
        }
        init(newScenario);
    }

    public void startPreviousScenario() {
        Scenario newScenario = getScenarioSuite(currentScenario).getPrevious(currentScenario);
        if (newScenario == null) {
            ScenarioSuite scenarioSuite = CollectionUtils.safeListAccess(scenarioSuites, getScenarioSuitIndex(currentScenario) - 1);
            newScenario = scenarioSuite.getLast();
        }
        init(newScenario);
    }

    public void restartCurrentScenario() {
        init(currentScenario);
    }

    public String getCurrentName() {
        return currentScenario.toString();
    }

    private ScenarioSuite getScenarioSuite(Scenario scenario) {
        for (ScenarioSuite scenarioSuite : scenarioSuites) {
            if (scenarioSuite.contains(scenario)) {
                return scenarioSuite;
            }
        }
        throw new IllegalArgumentException();
    }


    private int getScenarioSuitIndex(Scenario scenario) {
        for (int i = 0; i < scenarioSuites.size(); i++) {
            ScenarioSuite scenarioSuite = scenarioSuites.get(i);
            if (scenarioSuite.contains(scenario)) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    private Scenario findStartScenario() {
        Scenario start = null;
        for (ScenarioSuite scenarioSuite : scenarioSuites) {
            Scenario tmp = scenarioSuite.findStart();
            if (tmp != null) {
                if (start != null) {
                    System.err.println("More then one start Scenarion found: " + tmp);
                }
                start = tmp;
            }
        }
        if (start != null) {
            return start;
        } else {
            return scenarioSuites.get(0).getFirst();
        }
    }

    private void init(Scenario newScenario) {
        if (currentScenario != null) {
            currentScenario.stop();
        }
        currentScenario = newScenario;
        botService.killAllBots();
        gameEngine.stop();

        GameEngineConfig gameEngineConfig = setupGameEngineConfig();
        currentScenario.setupTerrain(gameEngineConfig.getPlanetConfig().getTerrainSlopePositions(), gameEngineConfig.getPlanetConfig().getTerrainObjectPositions());
        gameEngine.initialise(gameEngineConfig);
        currentScenario.setupBots(botService);
        gameEngine.start();
        UserContext userContext = new UserContext().setName("User 1").setLevelId(LEVEL_1_ID);
        PlayerBase playerBase = baseItemService.createHumanBase(userContext);
        currentScenario.setupSyncItems(baseItemService, playerBase, resourceService, boxService);
        List<AbstractBotCommandConfig> botCommandConfigs = new ArrayList<>();
        currentScenario.setupBotCommands(botCommandConfigs);
        botService.executeCommands(botCommandConfigs);
        QuestConfig questConfig = currentScenario.setupQuest();
        if (questConfig != null) {
            questService.activateCondition(userContext, questConfig);
        }
        currentScenario.executeCommands(commandService);
    }

    public GameEngineConfig setupGameEngineConfig() {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setGroundSkeletonConfig(setupGroundSkeletonConfig());
        gameEngineConfig.setSlopeSkeletonConfigs(setupSlopeSkeletonConfigs());
        gameEngineConfig.setTerrainObjectConfigs(setupTerrainObjectConfigs());
        gameEngineConfig.setLevelConfigs(setupLevels());
        gameEngineConfig.setBaseItemTypes(Arrays.asList(SIMPLE_FIX_ITEM_TYPE, SIMPLE_MOVABLE_ITEM_TYPE, HARVESTER_ITEM_TYPE, ATTACKER_ITEM_TYPE, BUILDER_ITEM_TYPE, FACTORY_ITEM_TYPE));
        gameEngineConfig.setResourceItemTypes(Collections.singletonList(RESOURCE_ITEM_TYPE));
        gameEngineConfig.setBoxItemTypes(Collections.singletonList(BOX_ITEM_TYPE));
        gameEngineConfig.setInventoryItems(Collections.singletonList(INVENTORY_ITEM));
        gameEngineConfig.setPlanetConfig(setupPlanetConfig());
        return gameEngineConfig;
    }

    private GroundSkeletonConfig setupGroundSkeletonConfig() {
        GroundSkeletonConfig groundSkeletonConfig = new GroundSkeletonConfig();
        groundSkeletonConfig.setHeightXCount(1);
        groundSkeletonConfig.setHeightYCount(1);
        groundSkeletonConfig.setHeights(new double[][]{{0.0}});
        groundSkeletonConfig.setSplattingXCount(1);
        groundSkeletonConfig.setSplattingYCount(1);
        groundSkeletonConfig.setSplattings(new double[][]{{0.0}});
        return groundSkeletonConfig;
    }

    private List<SlopeSkeletonConfig> setupSlopeSkeletonConfigs() {
        List<SlopeSkeletonConfig> slopeSkeletonConfigs = new ArrayList<>();
        SlopeSkeletonConfig slopeSkeletonConfig = new SlopeSkeletonConfig();
        slopeSkeletonConfig.setId(SLOPE_ID).setRows(2).setSegments(1).setHeight(1).setType(SlopeSkeletonConfig.Type.LAND).setVerticalSpace(0.2).setWidth(0.5);
        slopeSkeletonConfig.setSlopeNodes(new SlopeNode[][]{{new SlopeNode().setPosition(new Vertex(0, 0, 0)), new SlopeNode().setPosition(new Vertex(0.5, 0, 10))}});
        slopeSkeletonConfigs.add(slopeSkeletonConfig);
        return slopeSkeletonConfigs;
    }

    private List<TerrainObjectConfig> setupTerrainObjectConfigs() {
        List<TerrainObjectConfig> terrainObjectConfigs = new ArrayList<>();
        terrainObjectConfigs.add(new TerrainObjectConfig().setId(TERRAIN_OBJECT_ID).setRadius(4));
        return terrainObjectConfigs;
    }

    private PlanetConfig setupPlanetConfig() {
        PlanetConfig planetConfig = new PlanetConfig();
        planetConfig.setGroundMeshDimension(new Rectangle(-20, -20, 40, 40)).setWaterLevel(0).setHouseSpace(1000);
        planetConfig.setTerrainSlopePositions(new ArrayList<>());
        planetConfig.setTerrainObjectPositions(new ArrayList<>());
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(SIMPLE_MOVABLE_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(SIMPLE_FIX_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(HARVESTER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(ATTACKER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(BUILDER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(FACTORY_ITEM_TYPE.getId(), 1000);
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        return planetConfig;
    }

    private List<LevelConfig> setupLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(SIMPLE_MOVABLE_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(SIMPLE_FIX_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(HARVESTER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(ATTACKER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(BUILDER_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(FACTORY_ITEM_TYPE.getId(), 1000);
        levels.add(new LevelConfig().setLevelId(LEVEL_1_ID).setNumber(0).setItemTypeLimitation(itemTypeLimitation).setXp2LevelUp(10));
        return levels;
    }

    private void setupScenarioSuits() {
        addScenarioSuite(new MoveScenarioSuite());
        addScenarioSuite(new MoveStopScenarioSuite());
        addScenarioSuite(new MoveBypassFrontalScenarioSuite());
        addScenarioSuite(new MoveFixScenarioSuite());
        addScenarioSuite(new MoveVsMovingScenarioSuite());
        addScenarioSuite(new MoveVsStandingScenarioSuite());
        addScenarioSuite(new MoveSamePositionScenarioSuite());
        addScenarioSuite(new MoveOverlappingScenarioSuite());
        addScenarioSuite(new HarvestScenarioSuite());
        addScenarioSuite(new MoveObstacleScenarioSuite());
        addScenarioSuite(new MoveSingleOutOfGroupScenarioSuite());
        addScenarioSuite(new MoveTerrainObjectScenarioSuite());
        addScenarioSuite(new BotScenarioSuite());
        addScenarioSuite(new AttackScenarioSuite());
        addScenarioSuite(new PickBoxScenarioSuite());
        addScenarioSuite(new BuildScenarioSuite());
        addScenarioSuite(new FabricateScenarioSuite());
    }

    @Override
    public void onQuestPassed(UserContext examinee, QuestConfig questConfig) {
        System.out.println("************************************************");
        System.out.println("**************** Quest passed ******************");
        System.out.println("************************************************");
    }
}

