package com.btxtech.gameengine;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Rectangle;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.dto.SlopeNode;
import com.btxtech.shared.dto.SlopeSkeletonConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.GameEngine;
import com.btxtech.shared.gameengine.datatypes.PlayerBase;
import com.btxtech.shared.gameengine.datatypes.config.GameEngineConfig;
import com.btxtech.shared.gameengine.datatypes.config.LevelConfig;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalAreaConfig;
import com.btxtech.shared.gameengine.datatypes.itemtype.PhysicalMovableConfig;
import com.btxtech.shared.gameengine.planet.BaseItemService;
import com.btxtech.webglemulator.razarion.DevToolsSimpleExecutorServiceImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
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
    private static final int LEVEL_1_ID = 1;
    private static final int SLOPE_ID = 1;
    @Inject
    private GameEngine gameEngine;
    @Inject
    private BaseItemService baseItemService;
    @Inject
    private DevToolsSimpleExecutorServiceImpl devToolsSimpleExecutorService;
    private List<ScenarioProvider> scenes = new ArrayList<>();
    private int number = 0;
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture backgroundWorker;

    static {
        BaseItemType simpleMovable = new BaseItemType();
        simpleMovable.setHealth(100).setSpawnDurationMillis(1000);
        simpleMovable.setId(1);
        simpleMovable.setPhysicalAreaConfig(new PhysicalMovableConfig().setAcceleration(40).setSpeed(40).setMinTurnSpeed(40.0 * 0.2).setAngularVelocity(Math.toRadians(30)).setRadius(10));
        SIMPLE_MOVABLE_ITEM_TYPE = simpleMovable;

        BaseItemType simpleFix = new BaseItemType();
        simpleFix.setHealth(100).setSpawnDurationMillis(1000);
        simpleFix.setId(2);
        simpleFix.setPhysicalAreaConfig(new PhysicalAreaConfig().setRadius(10));
        SIMPLE_FIX_ITEM_TYPE = simpleFix;
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
        gameEngine.stop();

        GameEngineConfig gameEngineConfig = setupGameEngineConfig();
        ScenarioProvider scenarioProvider = scenes.get(number);
        scenarioProvider.setupTerrain(gameEngineConfig.getPlanetConfig().getTerrainSlopePositions(), gameEngineConfig.getPlanetConfig().getTerrainObjectPositions());
        gameEngine.initialise(gameEngineConfig);
        PlayerBase playerBase = baseItemService.createHumanBase(new UserContext().setName("User 1").setLevelId(LEVEL_1_ID));
        scenarioProvider.setupSyncItems(baseItemService, playerBase);

        this.number = number;
    }

    public GameEngineConfig setupGameEngineConfig() {
        GameEngineConfig gameEngineConfig = new GameEngineConfig();
        gameEngineConfig.setGroundSkeletonConfig(setupGroundSkeletonConfig());
        gameEngineConfig.setSlopeSkeletonConfigs(setupSlopeSkeletonConfigs());
        gameEngineConfig.setTerrainObjectConfigs(new ArrayList<>());
        gameEngineConfig.setLevelConfigs(setupLevels());
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
        slopeSkeletonConfig.setId(SLOPE_ID).setRows(2).setSegments(1).setHeight(100).setType(SlopeSkeletonConfig.Type.LAND).setVerticalSpace(10).setWidth(5);
        slopeSkeletonConfig.setSlopeNodes(new SlopeNode[][]{{new SlopeNode().setPosition(new Vertex(0, 0, 0)), new SlopeNode().setPosition(new Vertex(5, 0, 100))}});
        slopeSkeletonConfigs.add(slopeSkeletonConfig);
        return slopeSkeletonConfigs;
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
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-100, 0), destination);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 2
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), direction);
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
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-200, 0), new DecimalPosition(200, 0));
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
                DecimalPosition direction = new DecimalPosition(50, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), direction);
            }
        });
        // 5
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-60, 0), new DecimalPosition(0, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), null);
            }
        });
        // 6
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
            }
        });
        // Bypass frontal (bui1dings)
        // 7
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(50, 0), null);
            }
        });
        // 8
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), new DecimalPosition(100, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), null);
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(40, 0), null);
            }
        });
        // Moving units vs fix units
        // 9
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), new DecimalPosition(100, 0));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(30, 0), null);
            }
        });
        // 10
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 5), new DecimalPosition(100, 5));
                createSyncBaseItem(SIMPLE_FIX_ITEM_TYPE, new DecimalPosition(30, 0), null);
            }
        });
        // Moving against each other
        // 11
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 5), new DecimalPosition(100, 5));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), new DecimalPosition(-100, 0));
            }
        });
        // 12
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), new DecimalPosition(100, 0));
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), new DecimalPosition(-100, 0));
            }
        });
        // Moving vs standing
        // 13
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 5), null);
            }
        });
        // 14
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), null);
            }
        });
        // 15
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), null);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(50, 0), null);
            }
        });
        // 16
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-100, 0), new DecimalPosition(200, 0));
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-100, 10), new DecimalPosition(200, 10));
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), null);
                    }
                }
            }
        });
        // 17
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(100, 0), null);

            }
        });
        // 18
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x + 100, 20 * y), null);
                    }
                }
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x - 50, 20 * y), direction);
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
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), direction);
            }
        });
        // 20
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), direction);
            }
        });
        // 21
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), direction);
            }
        });
        // 22
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(30, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-10, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-30, 0), direction);
            }
        });
        // 23
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(0, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-80, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-60, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-40, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(40, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(60, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(80, 0), direction);
            }
        });
        // 24
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(30, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 10), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, -10), direction);
            }
        });
        // 25
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }
        });
        // Overlapping
        // 26
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(10, 0), direction);
            }
        });
        // Obstacle
        // 27
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, -200, 200, 400));
            }
        });
        // 28
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, 10, 200, 400));
            }
        });
        // 29
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(100, 0);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-100, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-80, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-60, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-40, 0), direction);
                // createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(-20, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(0, 0), direction);
                createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20, 0), direction);
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, -200, 200, 400));
            }
        });
        // 30
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, -75, 200, 150));
            }
        });
        // 31
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                DecimalPosition direction = new DecimalPosition(200, 0);
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), direction);
                    }
                }
            }

            @Override
            public void setupTerrain(List<TerrainSlopePosition> slopePositions, List<TerrainObjectPosition> terrainObjectPositions) {
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, 30, 200, 150));
                slopePositions.add(createRectangleSlope(SLOPE_ID, 50, -180, 200, 150));
            }
        });
        // Move single unit out of group
        // 32
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == 0 && y == 0) {
                            destination = new DecimalPosition(200, 0);
                        }
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 33
        scenes.add(new ScenarioProvider() {
            @Override
            public void createSyncItems() {
                for (int x = -2; x < 3; x++) {
                    for (int y = -2; y < 3; y++) {
                        DecimalPosition destination = null;
                        if (x == -2 && y == 0) {
                            destination = new DecimalPosition(200, 0);
                        }
                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y), destination);
                    }
                }
            }
        });
        // 34
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
//                        createSyncBaseItem(SIMPLE_MOVABLE_ITEM_TYPE, new DecimalPosition(20 * x, 20 * y).add(200, 200), new DecimalPosition(2700, 1700));
//                    }
//                }

            }
        });
    }

}

