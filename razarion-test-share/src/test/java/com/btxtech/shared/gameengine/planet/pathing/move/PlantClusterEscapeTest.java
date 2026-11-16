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
import com.btxtech.shared.gameengine.planet.terrain.DaggerTerrainServiceTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Units that already stand in a cluster of plants have to get out of it.
 * <p>
 * PROD 2026-11-15: six Vipers (radius 1) of a player at L10 stood in a cluster of ferns, banana plants
 * and palm bushes at 557-563 / 396-404, four of them overlapping a plant by a few centimetres. Every
 * move order got a path from the server to the next Spire, and none of them moved a metre: tapped
 * again and again for minutes.
 * <p>
 * Positions and radii are the ones of PROD, moved by -500 / -350 onto the test planet.
 */
public class PlantClusterEscapeTest extends DaggerTerrainServiceTestBase {
    private static final double OFFSET_X = -500;
    private static final double OFFSET_Y = -350;
    private static final int MAX_TICKS = 600;
    private static final boolean DEBUG = false;
    /** {x, y, radius} of the plants within 10 m of the Vipers, PROD planet 117. */
    private static final double[][] PLANTS = {
            {563, 396, 0.5}, {563, 399, 0.4}, {561, 397, 0.45}, {560, 403, 0.45}, {561, 401, 0.5}, {560, 400, 0.55},
            {560, 404, 0.55}, {563, 402, 0.45}, {563, 403, 0.4}, {559, 403, 0.55}, {557, 401, 0.45}};
    /** Where the six Vipers stood. */
    private static final double[][] VIPERS = {
            {559.80, 394.70}, {561.50, 395.70}, {559.60, 401.60}, {561.80, 399.70}, {559.60, 396.70}, {560.30, 398.50}};
    private final List<Circle2D> plants = new ArrayList<>();

    @Before
    public void before() {
        StaticGameConfig staticGameConfig = FallbackConfig.setupStaticGameConfig();
        // A Viper's footprint
        staticGameConfig.getBaseItemTypes().stream()
                .filter(type -> type.getId() == FallbackConfig.MOVING_TEST_ITEM_TYPE_ID)
                .forEach(type -> type.getPhysicalAreaConfig().setRadius(1.0));
        List<TerrainObjectConfig> configs = new ArrayList<>();
        List<TerrainObjectPosition> positions = new ArrayList<>();
        for (double[] plant : PLANTS) {
            int id = configs.size() + 1;
            configs.add(new TerrainObjectConfig().id(id).radius(plant[2]));
            DecimalPosition position = new DecimalPosition(plant[0] + OFFSET_X, plant[1] + OFFSET_Y);
            positions.add(new TerrainObjectPosition().id(id).terrainObjectConfigId(id).scale(new Vertex(1, 1, 1)).position(position));
            plants.add(new Circle2D(position, plant[2]));
        }
        staticGameConfig.setTerrainObjectConfigs(configs);
        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(512, 512));
        setupEnvironment(staticGameConfig, planetConfig);
        getTestNativeTerrainShapeAccess().setPlanetConfig(planetConfig);
        getTestNativeTerrainShapeAccess().setTerrainObjectPositions(positions);
        getPlanetService().initialise(getPlanetConfig(), GameEngineMode.MASTER, setupMasterPlanetConfig(), null, () -> getPlanetService().start(), null);
    }

    @Test
    public void vipersStuckInPlantsDriveOut() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), java.util.Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        playerBase.setResources(Double.MAX_VALUE);
        List<SyncPhysicalMovable> units = new ArrayList<>();
        // The order to the next Spire, north-east of the cluster
        DecimalPosition destination = new DecimalPosition(140, 120);
        for (double[] viper : VIPERS) {
            SyncBaseItem unit = spawn(new DecimalPosition(viper[0] + OFFSET_X, viper[1] + OFFSET_Y), playerBase);
            SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
            com.btxtech.shared.gameengine.datatypes.command.SimplePath simplePath = getPathingService().setupPathToDestination(unit, destination.add(units.size() * 3, 0));
            if (DEBUG) System.out.println("DBG unit " + units.size() + " path " + simplePath.getWayPositions().subList(0, Math.min(4, simplePath.getWayPositions().size())) + " n=" + simplePath.getWayPositions().size());
            movable.setPath(simplePath);
            units.add(movable);
        }

        List<DecimalPosition> starts = new ArrayList<>();
        units.forEach(unit -> starts.add(unit.getPosition()));
        int ticks = 0;
        while (units.stream().anyMatch(SyncPhysicalMovable::hasDestination) && ticks < MAX_TICKS) {
            tickPlanetService();
            ticks++;
            if (DEBUG && ticks <= 12) {
                SyncPhysicalMovable u = units.get(1);
                System.out.println("DBG tick " + ticks + " pos " + u.getPosition() + " pref " + u.getPreferredVelocity() + " vel " + u.getVelocity() + " wp " + (u.getPath() != null ? u.getPath().getCurrentWayPoint() : null) + " crowded " + u.isCrowded());
            }
        }

        StringBuilder report = new StringBuilder();
        for (int i = 0; i < units.size(); i++) {
            SyncPhysicalMovable unit = units.get(i);
            report.append("\n  unit ").append(i).append(" from ").append(starts.get(i)).append(" to ").append(unit.getPosition())
                    .append(" moved ").append(String.format("%.1f", unit.getPosition().getDistance(starts.get(i))))
                    .append(unit.hasDestination() ? " STILL MOVING" : "").append(unit.isDestinationUnreachable() ? " UNREACHABLE" : "");
        }
        for (SyncPhysicalMovable unit : units) {
            Assert.assertTrue("not out of the plants after " + ticks + " ticks:" + report, unit.getPosition().getDistance(new DecimalPosition(61, 49)) > 15);
        }
    }

    @Test
    public void eachViperAloneDrivesOut() {
        UserContext userContext = createLevel1UserContext();
        PlayerBaseFull playerBase = getBaseItemService().createHumanBase(0, userContext.getLevelId(), java.util.Collections.emptyMap(), userContext.getUserId(), userContext.getName());
        spawn(new DecimalPosition(400, 400), playerBase); // keeps the base alive between the units
        StringBuilder report = new StringBuilder();
        boolean allOut = true;
        for (double[] viper : VIPERS) {
            SyncBaseItem unit = spawn(new DecimalPosition(viper[0] + OFFSET_X, viper[1] + OFFSET_Y), playerBase);
            SyncPhysicalMovable movable = (SyncPhysicalMovable) unit.getAbstractSyncPhysical();
            DecimalPosition start = movable.getPosition();
            movable.setPath(getPathingService().setupPathToDestination(unit, new DecimalPosition(140, 120)));
            int ticks = 0;
            while (movable.hasDestination() && ticks < MAX_TICKS) {
                tickPlanetService();
                ticks++;
            }
            boolean out = movable.getPosition().getDistance(new DecimalPosition(61, 49)) > 15;
            allOut &= out;
            report.append(System.lineSeparator()).append("  from ").append(start).append(" moved ").append(String.format("%.1f", movable.getPosition().getDistance(start))).append(out ? " out" : " STUCK");
            getBaseItemService().removeSyncItem(unit);
        }
        Assert.assertTrue(report.toString(), allOut);
    }

    private SyncBaseItem spawn(DecimalPosition position, PlayerBaseFull playerBase) {
        try {
            return getBaseItemService().spawnSyncBaseItem(getTestShareDagger().itemTypeService().getBaseItemType(FallbackConfig.MOVING_TEST_ITEM_TYPE_ID), position, 0, playerBase, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
