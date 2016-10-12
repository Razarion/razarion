package com.btxtech.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.AbstractBotCommandConfig;
import com.btxtech.shared.dto.BotHarvestCommandConfig;
import com.btxtech.shared.dto.BotMoveCommandConfig;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlaceConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotEnragementStateConfig;
import com.btxtech.shared.gameengine.datatypes.config.bot.BotItemConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.ResourceItemType;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.shared.gameengine.planet.ResourceService;
import com.btxtech.shared.gameengine.planet.bot.BotService;
import com.btxtech.webglemulator.razarion.DevToolsSimpleExecutorServiceImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ScenarioService {
    private static final BaseItemType SIMPLE_MOVABLE_ITEM_TYPE;
    private static final BaseItemType SIMPLE_FIX_ITEM_TYPE;
    private static final BaseItemType HARVESTER_ITEM_TYPE;
    private static final ResourceItemType RESOURCE_ITEM_TYPE;
    private static final int LEVEL_1_ID = 1;
    private static final int SLOPE_ID = 1;
    private static final int TERRAIN_OBJECT_ID = 1;
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
    private List<ScenarioProvider> scenes = new ArrayList<>();
    private int number = 38;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture backgroundWorker;

    static {
        int itemId = 0;
        BaseItemType simpleMovable = new BaseItemType();
        simpleMovable.setHealth(100).setSpawnDurationMillis(1000);
        simpleMovable.setId(++itemId);
        simpleMovable.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        SIMPLE_MOVABLE_ITEM_TYPE = simpleMovable;

        BaseItemType simpleFix = new BaseItemType();
        simpleFix.setHealth(100).setSpawnDurationMillis(1000);
        simpleFix.setId(++itemId);
        simpleFix.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(2));
        SIMPLE_FIX_ITEM_TYPE = simpleFix;

        BaseItemType harvester = new BaseItemType();
        harvester.setHealth(100).setSpawnDurationMillis(1000);
        harvester.setId(++itemId);
        harvester.setPhysicalAreaConfig(new PhysicalAreaConfig().setAcceleration(2.78).setSpeed(17.0).setMinTurnSpeed(17.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(2));
        harvester.setHarvesterType(new HarvesterType().setProgress(10).setRange(4));
        HARVESTER_ITEM_TYPE = harvester;

        ResourceItemType resource = new ResourceItemType();
        resource.setRadius(2).setAmount(1000).setId(++itemId);
        RESOURCE_ITEM_TYPE = resource;
    }

    @PostConstruct
    public void postConstruct() {
        setupSzenarios();
    }

    public void initNext() {
        int nextNumber;
        nextNumber = number + 1;
        if (nextNumber > scenes.size() - 1) {
            nextNumber = 0;
        }
        init(nextNumber);
    }

    public void initPrevious() {
        int nextNumber;
        nextNumber = number - 1;
        if (nextNumber < 0) {
            nextNumber = scenes.size() - 1;
        }
        init(nextNumber);
    }

    public void initCurrent() {
        init(number);
    }

    public int getNumber() {
        return number;
    }

    private void init(int number) {
        if (backgroundWorker != null) {
            backgroundWorker.cancel(true);
            backgroundWorker = null;
        }
        botService.killAllBots();
        gameEngine.stop();

        GameEngineConfig gameEngineConfig = setupGameEngineConfig();
        ScenarioProvider scenarioProvider = scenes.get(number);
        scenarioProvider.setupTerrain(gameEngineConfig.getPlanetConfig().getTerrainSlopePositions(), gameEngineConfig.getPlanetConfig().getTerrainObjectPositions());
        gameEngine.initialise(gameEngineConfig);
        Collection<BotConfig> botConfigs = new ArrayList<>();
        scenarioProvider.setupBots(botConfigs);
        botService.startBots(botConfigs);
        gameEngine.start();
        PlayerBase playerBase = baseItemService.createHumanBase(new UserContext().setName("User 1").setLevelId(LEVEL_1_ID));
        scenarioProvider.setupSyncItems(baseItemService, playerBase, resourceService);
        List<AbstractBotCommandConfig> botCommandConfigs = new ArrayList<>();
        scenarioProvider.setupBotCommands(botCommandConfigs);
        botService.executeCommands(botCommandConfigs);

        this.number = number;
    }

    public GameEngineConfig setupGameEngineConfig() {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setGroundSkeletonConfig(setupGroundSkeletonConfig());
        gameEngineConfig.setSlopeSkeletonConfigs(setupSlopeSkeletonConfigs());
        gameEngineConfig.setTerrainObjectConfigs(setupTerrainObjectConfigs());
        gameEngineConfig.setLevelConfigs(setupLevels());
        gameEngineConfig.setBaseItemTypes(Arrays.asList(SIMPLE_FIX_ITEM_TYPE, SIMPLE_MOVABLE_ITEM_TYPE, HARVESTER_ITEM_TYPE));
        gameEngineConfig.setResourceItemTypes(Collections.singletonList(RESOURCE_ITEM_TYPE));
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
        planetConfig.setItemTypeLimitation(itemTypeLimitation);
        return planetConfig;
    }

    private List<LevelConfig> setupLevels() {
        List<LevelConfig> levels = new ArrayList<>();
        Map<Integer, Integer> itemTypeLimitation = new HashMap<>();
        itemTypeLimitation.put(SIMPLE_MOVABLE_ITEM_TYPE.getId(), 1000);
        itemTypeLimitation.put(SIMPLE_FIX_ITEM_TYPE.getId(), 1000);
        levels.add(new LevelConfig().setLevelId(LEVEL_1_ID).setNumber(0).setItemTypeLimitation(itemTypeLimitation));
        return levels;
    }

    private void setupSzenarios() {
        // Simple move
        //0
        scenes.add(new ScenarioProvider() {

            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(-100, 0));
            }
        });
        // Stop condition
        //1
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition destination = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), destination);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), destination);
                    }
                }
            }
        });
        // 2
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
            }
        });
        // 3
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                backgroundWorker = scheduler.scheduleAtFixedRate((Runnable) () -> {
                    try {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), new DecimalPosition(20, 0));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }, 1000, 1000, TimeUnit.MILLISECONDS);
            }
        });
        // 4
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(5, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), direction);
            }
        });
        // 5
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), new DecimalPosition(0, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), null);
            }
        });
        // 6
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(10, 0));
            }
        });
        // Bypass frontal (bui1dings)
        // 7
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(10, 0));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(5, 0), null);
            }
        });
        // 8
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), new DecimalPosition(20, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), null);
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(8, 0), null);
            }
        });
        // Moving units vs fix units
        // 9
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(20, 0));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(6, 0), null);
            }
        });
        // 10
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 1), new DecimalPosition(20, 1));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(6, 0), null);
            }
        });
        // Moving against each other
        // 11
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 10), new DecimalPosition(20, 10));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), new DecimalPosition(-20, 0));
            }
        });
        // 12
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-3, 0), new DecimalPosition(10, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(3, 0), new DecimalPosition(-10, 0));
            }
        });
        // Moving vs standing
        // 13
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 1), null);
            }
        });
        // 14
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(10, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), null);
            }
        });
        // 15
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(30, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), null);
            }
        });
        // 16
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), new DecimalPosition(50, 0));
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 10), new DecimalPosition(20, 10));
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), null);
                    }
                }
            }
        });
        // 17
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), null);

            }
        });
        // 18
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x + 10, 4 * y), null);
                    }
                }
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x - 20, 4 * y), direction);
                    }
                }
            }
        });
        // Move to same position
        // 19
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-3, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(3, 0), direction);
            }
        });
        // 20
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
            }
        });
        // 21
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
            }
        });
        // 22
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(6, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), direction);
            }
        });
        // 23
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-16, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-12, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-8, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(8, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(12, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(16, 0), direction);
            }
        });
        // 24
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(3, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), direction);
            }
        });
        // 25
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
            }
        });
        // Overlapping
        // 26
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(1, 0), direction);
            }
        });
        // Resources
        // 27
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncResourceItem(RESOURCE_ITEM_TYPE, new DecimalPosition(10, 0));
            }
        });
        // Obstacle
        // 28
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(10, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 5, -20, 20, 40));
            }
        });
        // 29
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(10, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 5, 1, 20, 40));
            }
        });
        // 30
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-8, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-6, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-4, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-2, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 10, -20, 20, 40));
            }
        });
        // 31
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(20, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 20, -7.5, 20, 15));
            }
        });
        // 32
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(50, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 15, 5, 20, 15));
                slopePositions.add(createRectangleSlope(SLOPE_ID, 15, -20, 20, 15));
            }
        });
        // Move single unit out of group
        // 33
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == 0 && y == 0) {
                            destination = new DecimalPosition(50, 0);
                        }
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), destination);
                    }
                }
            }
        });
        // 34
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == -2 && y == 0) {
                            destination = new DecimalPosition(50, 0);
                        }
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(4 * x, 4 * y), destination);
                    }
                }
            }
        });
        // 35
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                // Terrain
//                TerrainUiService terrainUiService = GameMock.startTerrainSurface("/SlopeSkeletonSlope.json", "/SlopeSkeletonBeach.json", "/GroundSkeleton.json", "/TerrainSlopePositions.json");
//                Collection<Obstacle> obstacles = terrainUiService.getAllObstacles();
//                for (Obstacle obstacle : obstacles) {
//                    pathingService.addObstacle(obstacle);
//                }
//                // Units
//                for (int x = -2; x < 3; x++) {
//                    for (int y = -2; y < 3; y++) {
//                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(2 * x, 2 * y).add(20, 20), new DecimalPosition(2700, 1700));
//                    }
//                }

            }
        });
        // Terrain objects
        // 36
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(20, 0));
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(TERRAIN_OBJECT_ID).setPosition(new DecimalPosition(10, 0)));
            }
        });
        // 37
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(20, 0));
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                terrainObjectPositions.add(new TerrainObjectPosition().setId(1).setTerrainObjectId(TERRAIN_OBJECT_ID).setPosition(new DecimalPosition(10, 5)));
            }
        });
        // Bot
        // 38
        scenes.add(new ScenarioProvider() {
            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(SIMPLE_MOVABLE_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotMoveCommandConfig().setBotId(1).setDecimalPosition(new DecimalPosition(0, 20)).setBaseItemTypeId(SIMPLE_MOVABLE_ITEM_TYPE.getId()));
            }
        });
        // 39
        scenes.add(new ScenarioProvider() {
            @Override
            protected void createSyncItems() {
                createSyncResourceItem(RESOURCE_ITEM_TYPE, new DecimalPosition(20, 0));
            }

            @Override
            public void setupBots(Collection<BotConfig> botConfigs) {
                List<BotEnragementStateConfig> botEnragementStateConfigs = new ArrayList<>();
                List<BotItemConfig> botItems = new ArrayList<>();
                botItems.add(new BotItemConfig().setBaseItemTypeId(HARVESTER_ITEM_TYPE.getId()).setCount(1).setCreateDirectly(true).setPlace(new PlaceConfig().setPosition(new DecimalPosition(0, 0))).setNoSpawn(true));
                botEnragementStateConfigs.add(new BotEnragementStateConfig().setName("Normal").setBotItems(botItems));
                botConfigs.add(new BotConfig().setId(1).setActionDelay(3000).setBotEnragementStateConfigs(botEnragementStateConfigs).setName("Kenny").setNpc(true));
            }

            @Override
            public void setupBotCommands(Collection<AbstractBotCommandConfig> botCommandConfigs) {
                botCommandConfigs.add(new BotHarvestCommandConfig().setBotId(1).setResourceItemTypeId(RESOURCE_ITEM_TYPE.getId()).setResourceSelection(new PlaceConfig().setPosition(new DecimalPosition(20, 0))).setHarvesterItemTypeId(HARVESTER_ITEM_TYPE.getId()));
            }
        });

    }

}

