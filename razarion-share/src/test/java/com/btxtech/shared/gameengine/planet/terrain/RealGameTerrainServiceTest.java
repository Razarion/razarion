package com.btxtech.shared.gameengine.planet.terrain;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.dto.DrivewayConfig;
import com.btxtech.shared.dto.FallbackConfig;
import com.btxtech.shared.dto.SlopeShape;
import com.btxtech.shared.dto.TerrainSlopeCorner;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.datatypes.config.SlopeConfig;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 14.11.2017.
 */
@Ignore
public class RealGameTerrainServiceTest extends WeldTerrainServiceTestBase {

    private void setup(List<SlopeConfig> slopeConfigs, List<TerrainSlopePosition> terrainSlopePositions) {
        PlanetConfig planetConfig = FallbackConfig.setupPlanetConfig();
        planetConfig.setSize(new DecimalPosition(5120, 512));

        setupTerrainTypeService(slopeConfigs, null, null, null, planetConfig, terrainSlopePositions, null, null, null);

    }

    @Test
    public void test() {
// ---------------------------SlopeConfig---------------------------
        List<SlopeConfig> slopeConfigs = Collections.singletonList(new SlopeConfig()
                .id(1)
                .horizontalSpace(5.0)
                .groundConfigId(253)
                .waterConfigId(null)
                .outerLineGameEngine(1.0)
                .innerLineGameEngine(1.0)
                .slopeShapes(Arrays.asList(
                        new SlopeShape().slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(0.000, 1.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.000, 1.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.000, 3.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.500, 3.000)).slopeFactor(1.0),
                        new SlopeShape().position(new DecimalPosition(1.500, 2.800)).slopeFactor(1.0))));
// ---------------------------DrivewayConfig---------------------------
        List<DrivewayConfig> drivewayConfigs = Arrays.asList(
                new DrivewayConfig().id(31).angle(0.3490658));
// ---------------------------Slope corners---------------------------
        List<TerrainSlopePosition> terrainSlopePositions = Collections.singletonList(new TerrainSlopePosition()
                .id(21)
                .slopeConfigId(1)
                .polygon(Arrays.asList(
                        GameTestHelper.createTerrainSlopeCorner(260.75799076593205, 359.4122733368791, 31),
                        GameTestHelper.createTerrainSlopeCorner(263.6405429803556, 362.2948255513026, null),
                        GameTestHelper.createTerrainSlopeCorner(280.2008759044115, 345.73449262724677, null),
                        GameTestHelper.createTerrainSlopeCorner(340.2008759044115, 405.73449262724677, null),
                        GameTestHelper.createTerrainSlopeCorner(340.1641636418335, 405.77120488982473, null),
                        GameTestHelper.createTerrainSlopeCorner(397.56815350169376, 463.1751947496849, null),
                        GameTestHelper.createTerrainSlopeCorner(397.6799534428304, 463.0633948085483, null),
                        GameTestHelper.createTerrainSlopeCorner(457.6799534428304, 523.0633948085483, null),
                        GameTestHelper.createTerrainSlopeCorner(397.6799534428304, 583.0633948085483, null),
                        GameTestHelper.createTerrainSlopeCorner(397.64479839075983, 583.0282397564777, null),
                        GameTestHelper.createTerrainSlopeCorner(395.27882944073747, 585.3942087065001, null),
                        GameTestHelper.createTerrainSlopeCorner(340.8422879124359, 530.9576671781984, null),
                        GameTestHelper.createTerrainSlopeCorner(285.03037385366804, 586.7695812369664, null),
                        GameTestHelper.createTerrainSlopeCorner(279.4669153819696, 581.2061227652679, null),
                        GameTestHelper.createTerrainSlopeCorner(279.55537758547104, 581.1176605617666, null),
                        GameTestHelper.createTerrainSlopeCorner(219.75230545841498, 521.3145884347105, null),
                        GameTestHelper.createTerrainSlopeCorner(277.72992632256717, 463.3369675705583, null),
                        GameTestHelper.createTerrainSlopeCorner(277.64983068871896, 463.2568719367101, null),
                        GameTestHelper.createTerrainSlopeCorner(277.6865429512969, 463.22015967413216, null),
                        GameTestHelper.createTerrainSlopeCorner(220.20087590441148, 405.73449262724677, null),
                        GameTestHelper.createTerrainSlopeCorner(242.93978865473218, 382.99557987692606, null),
                        GameTestHelper.createTerrainSlopeCorner(240.00411168683075, 380.05990290902463, 31),
                        GameTestHelper.createTerrainSlopeCorner(248.00411168683075, 372.05990290902463, 31),
                        GameTestHelper.createTerrainSlopeCorner(248.01029680117085, 372.0660880233647, 31),
                        GameTestHelper.createTerrainSlopeCorner(254.9955059204925, 365.0808789040431, 31),
                        GameTestHelper.createTerrainSlopeCorner(255.0424455596303, 365.1278185431809, 31))));
        setupTerrainTypeService(slopeConfigs, drivewayConfigs, null, null, null, terrainSlopePositions, null, null, null);
        showDisplay();
    }

    private List<DecimalPosition> toLine(TerrainSlopeCorner[] terrainSlopeCorners) {
        return Arrays.stream(terrainSlopeCorners).map(TerrainSlopeCorner::getPosition).collect(Collectors.toList());
    }

    private void currentWayPoint(Path path, DecimalPosition position, DecimalPosition wayPosition, DecimalPosition velocity, DecimalPosition preferredVelocity) {
        path.addPosition(position);
        path.addWayPoint(wayPosition);
    }

    public static class Path {
        List<DecimalPosition> positions = new ArrayList<>();
        List<DecimalPosition> wayPoints = new ArrayList<>();

        public void addPosition(DecimalPosition position) {
            positions.add(position);
        }

        public List<DecimalPosition> getPositions() {
            return positions;
        }

        public void addWayPoint(DecimalPosition wayPoint) {
            wayPoints.add(wayPoint);
        }

        public List<DecimalPosition> getWayPoints() {
            return wayPoints;
        }
    }
}
