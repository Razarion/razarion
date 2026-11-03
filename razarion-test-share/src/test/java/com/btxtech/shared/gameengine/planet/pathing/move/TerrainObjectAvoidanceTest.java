package com.btxtech.shared.gameengine.planet.pathing.move;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.UserContext;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.TerrainObjectConfig;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.gameengine.datatypes.GameEngineMode;
import com.btxtech.shared.gameengine.datatypes.PlayerBaseFull;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.StaticGameConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.Obstacle;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleTerrainObject;
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Terrain objects as ORCA obstacles (TerrainAnalyzer.getObstacles).
 * <p>
 * Without them ORCA kept units apart from each other only: six Vipers sent through the wood next to
 * a Spire shoved each other into the trees, the way point choice flipped for the units standing in
 * one, and server and client drove apart - 15-35 m corrections every two seconds (local play test
 * 2026-11-03).
 */
public class TerrainObjectAvoidanceTest extends DaggerTerrainServiceTestBase {
    private static final double TREE_RADIUS = 1.5;
    private static final int MAX_TICKS = 400;
    /** The collision branch of ORCA takes an overlap out in the next tick: what is left is numerical. */
    private static final double MAX_PENETRATION = 0.1;
    private final List<Circle2D> trees = new ArrayList<>();

    @Before
    public void before() {
        StaticGameConfig staticGameConfig = FallbackConfig.setupStaticGameConfig();
        staticGameConfig.setTerrainObjectConfigs(Collections.singletonList(new TerrainObjectConfig().id(1).radius(TREE_RADIUS)));
        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5120, 512));
        List<TerrainObjectPosition> terrainObjectPositions = new ArrayList<>();
        // A wood of 3 x 7 trees, 12 m apart: a gap A* fits a unit through, not two side by side
        for (int x = 236; x <= 260; x += 12) {
            for (int y = 54; y <= 126; y += 12) {
                DecimalPosition position = new DecimalPosition(x, y);
                terrainObjectPositions.add(new TerrainObjectPosition().id(terrainObjectPositions.size() + 1).terrainObjectConfigId(1).scale(new Vertex(1, 1, 1)).position(position));
                trees.add(new Circle2D(position, TREE_RADIUS));
            }
        }
        setupEnvironment(staticGameConfig, planetConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getTestNativeTerrainShapeAccess().setTerrainObjectPositions(terrainObjectPositions);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), null, () -> getPlanetService().start(), null);
    }

    @Test
    public void obstaclesAreTheTreesNearby() {
        Set<Obstacle> obstacles = getTerrainService().getTerrainAnalyzer().getObstacles(new DecimalPosition(242, 60), 8);

        List<Circle2D> circles = new ArrayList<>();
        obstacles.forEach(obstacle -> circles.add(((ObstacleTerrainObject) obstacle).getCircle()));
        // The four trees around it 8.5 - 1.5 = 7 m away, the next ones 17.5 m
        Assert.assertEquals(4, circles.size());
        Assert.assertTrue(circles.contains(new Circle2D(new DecimalPosition(236, 54), TREE_RADIUS)));
        Assert.assertTrue(circles.contains(new Circle2D(new DecimalPosition(248, 54), TREE_RADIUS)));
        Assert.assertTrue(circles.contains(new Circle2D(new DecimalPosition(236, 66), TREE_RADIUS)));
        Assert.assertTrue(circles.contains(new Circle2D(new DecimalPosition(248, 66), TREE_RADIUS)));
        Assert.assertTrue(getTerrainService().getTerrainAnalyzer().getObstacles(new DecimalPosition(200, 90), 5).isEmpty());
    }

    @Test
    public void aGroupDrivesThroughAWoodWithoutEnteringATree() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);
        List<SyncPhysicalMovable> units = new ArrayList<>();
        for (int column = 0; column < 2; column++) {
            for (int row = 0; row < 3; row++) {
                SyncBaseItem unit = spawn(new DecimalPosition(205 + column * 6, 84 + row * 6), playerBase);
                SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
                movable.setPath(getPathingService().setupPathToDestination(unit, new DecimalPosition(295 + column * 6, 84 + row * 6)));
                units.add(movable);
            }
        }

        double maxPenetration = 0;
        String worst = "-";
        int ticks = 0;
        while (units.stream().anyMatch(SyncPhysicalMovable::hasDestination) && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
            for (SyncPhysicalMovable unit : units) {
                for (Circle2D tree : trees) {
                    double penetration = unit.getRadius() + tree.getRadius() - unit.getPosition().getDistance(tree.getCenter());
                    if (penetration > maxPenetration) {
                        maxPenetration = penetration;
                        worst = "unit " + unit.getSyncItem().getId() + " at " + unit.getPosition() + " in tree " + tree.getCenter() + " tick " + ticks;
                    }
                }
            }
        }

        Assert.assertTrue("penetration " + maxPenetration + ": " + worst, maxPenetration <= MAX_PENETRATION);
        for (SyncPhysicalMovable unit : units) {
            Assert.assertFalse("still moving after " + ticks + " ticks at " + unit.getPosition(), unit.hasDestination());
            Assert.assertFalse("gave up at " + unit.getPosition(), unit.isDestinationUnreachable());
            Assert.assertTrue("stopped short at " + unit.getPosition(), unit.getPosition().getX() > 280);
        }
    }

    private SyncBaseItem spawn(DecimalPosition position, PlayerBaseFull playerBase) {
        try {
            return getBaseItemService().spawnSyncBaseItem(getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID), position, 0, playerBase, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
