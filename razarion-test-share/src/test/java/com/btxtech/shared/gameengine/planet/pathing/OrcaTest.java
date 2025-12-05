package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.AbstractSyncPhysical;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.ShareTestGuiDisplay;
import javafx.scene.paint.Color;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.shared.gameengine.planet.GameTestHelper.createObstacleSlopes;

/**
 * Created by Beat
 * on 24.05.2018.
 */
public class OrcaTest {
    @Test
    public void sameDirection1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void sameDirection2() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void sameDirection3() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(24, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void sameTarget1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, -5).normalize(10), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 16), new DecimalPosition(10, 5).normalize(10), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void against1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 20), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(9.165, -4.000));
    }

    @Test
    public void cutOff1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(18, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 23.5), new DecimalPosition(10, -5).normalize(10), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 16.5), new DecimalPosition(10, 5).normalize(10), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(6.491, 0));
    }

    @Test
    public void cutOff2() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(22.53334828640062, 20.0), new DecimalPosition(16.914085744083103, 0.0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(24.15842424362614, 16.0757879718212), new DecimalPosition(16.958178115101212, 1.1917193530786572), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(24.15842424362614, 23.9242120281788), new DecimalPosition(16.958178115101212, -1.1917193530786572), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20.0666779111833, 24.17954732809343), new DecimalPosition(16.958812996787714, -1.1826503033374522), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(16.268, 0));
    }

    @Test
    public void cutOff3() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0).normalize(10), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 24), new DecimalPosition(30, -5).normalize(10), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 16), new DecimalPosition(30, 5).normalize(10), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(0.0, 0));
    }

    @Test
    public void against2Middle() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 18), new DecimalPosition(-10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 22), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void against2Above() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 22.5), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 18), new DecimalPosition(-10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 22), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(9.355, 3.532));
    }

    @Test
    public void against2AboveFail() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 22), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 18), new DecimalPosition(-10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 22), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(0, 0));
    }

    @Test
    public void against2HoleMiddle() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 24), new DecimalPosition(-10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 16), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10, 0));
    }

    @Test
    public void against2HoleAbove() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(30, 23.5), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 24), new DecimalPosition(-10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 16), new DecimalPosition(-10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(9.27, -3.5));
    }

    @Test
    public void overlap1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(23.25, 18.3), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10.0, 0.0));
    }

    @Test
    public void overlap2() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(23.25, 18.3), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(9.596, 2.812));
    }

    @Test
    public void overlap3() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(22.5, 20.0), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10.0, 0.0));
    }

    @Test
    public void overlap4() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(19.045454545454547, 20.09090909090909), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(22.5, 20.0), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(7.453, 6.667));
    }

    @Test
    public void overlap5() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 23), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(8.944, 4.472));
    }

    @Test
    public void overlap6() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(21, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 17), new DecimalPosition(10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 23), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(10.0, 0.0));
    }

    @Test
    public void overlap7() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(19, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 17), new DecimalPosition(10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 23), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(-3.246, 0.0));
    }

    @Test
    public void overlap8() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(10, 0), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 16.1), new DecimalPosition(10, 0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20, 23.9), new DecimalPosition(10, 0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(0.0, 0.0));
    }

    @Test
    public void overlapUnmovable1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20.28207321464821, 23.86211363974772), new DecimalPosition(0.19964944493158018, 0.011836348191563963), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createAbstractSyncPhysical(3.0, TerrainType.LAND, new DecimalPosition(24.5927935691167, 25.56605099410689)));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(-0.18463990822690954, -0.07686419380933122));
    }

    @Test
    public void unknown1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(19.300187279685865, 23.7697593332751), new DecimalPosition(7.7869654150622845, -0.26517646985986776), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(17.9, 20.0), new DecimalPosition(6.5, 0.0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(17.636898286437166, 27.56109120396766), new DecimalPosition(8.979692233285652, -0.6042577227221333), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(21.9, 20.0), new DecimalPosition(6.5, 0.0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(7.787, -0.266));
    }

    @Test
    public void unknown2() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(17.89750480592674, 23.86212621643338), new DecimalPosition(6.496145932359411, -0.22380354217547135), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(16.79173066808286, 27.68886128334947), new DecimalPosition(8.480463023661791, -0.5759747427666616), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(15.8, 20.0), new DecimalPosition(4.5, 0.0), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20.25, 20.0), new DecimalPosition(5.0, 0.0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(6.496, -0.224));
    }

    @Test
    public void ignorePusherFromBehind() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(20.5476892279602, 23.85557052271858), new DecimalPosition(6.99635651438783, -0.2258218846413296), null);
        List<AbstractSyncPhysical> physicalAreas = new ArrayList<>();
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(19.140697624168546, 27.64447114848093), new DecimalPosition(8.61542501685567, -0.5449342059616612), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(16.340678708083146, 23.86980196862154), new DecimalPosition(6.806872705033804, -0.21301466900269897), null));
        physicalAreas.add(GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(22.75, 20.0), new DecimalPosition(5.5, 0.0), null));

        runTest(protagonist, physicalAreas, null, new DecimalPosition(6.996, -0.226));
    }

    @Test
    public void testSlope1() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), null);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(20, 20),
                new DecimalPosition(60, 20),
                new DecimalPosition(60, 60),
                new DecimalPosition(100, 60),
                new DecimalPosition(100, 65),
                new DecimalPosition(55, 65),
                new DecimalPosition(55, 25),
                new DecimalPosition(20, 25)
        ));
        runTest(protagonist, null, obstacles, new DecimalPosition(10.0, 10.0));
    }

    @Test
    public void testSlope2() {
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), null);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(40.3, 20.0),
                new DecimalPosition(51.7, 20.2),
                new DecimalPosition(65.9, 21.2),
                new DecimalPosition(77.49999999999999, 24.0),
                new DecimalPosition(89.29999999999998, 36.2),
                new DecimalPosition(91.1, 45.2),
                new DecimalPosition(88.3, 57.80000000000001),
                new DecimalPosition(76.9, 68.6),
                new DecimalPosition(63.7, 71.4),
                new DecimalPosition(41.699999999999996, 69.2),
                new DecimalPosition(32.89999999999999, 61.39999999999999),
                new DecimalPosition(26.699999999999996, 51.2),
                new DecimalPosition(33.3, 33.2)
        ));
        runTest(protagonist, null, obstacles, new DecimalPosition(10.0, 10.0));
    }

    @Test
    public void testSlope3() {
        DecimalPosition vAndPreferred = new DecimalPosition(1, 1).normalize(17);
        SyncPhysicalMovable protagonist = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(17, 20), vAndPreferred, null);
        List<ObstacleSlope> obstacles = createObstacleSlopes(
                new DecimalPosition(20, 20),
                new DecimalPosition(40, 20),
                new DecimalPosition(40, 40),
                new DecimalPosition(20, 40),
                new DecimalPosition(20, 35),
                new DecimalPosition(20, 30),
                new DecimalPosition(20, 25)
        );
        runTest(protagonist, null, obstacles, new DecimalPosition(2.0, 16.882));
    }

    private void runTest(SyncPhysicalMovable protagonist, List<AbstractSyncPhysical> physicalAreas, List<ObstacleSlope> obstacles, DecimalPosition expected) {
        Orca orca = new Orca(protagonist);
        if (physicalAreas != null) {
            physicalAreas.forEach(orca::add);
        }
        if (obstacles != null) {
            obstacles.forEach(orca::add);
        }
        orca.solve();
        // display(orca, protagonist, obstacles, physicalAreas);
        TestHelper.assertDecimalPosition("New velocity wrong", expected, orca.getNewVelocity());
    }

    @SuppressWarnings("unused")
    private void display(Orca orca, SyncPhysicalMovable protagonist, List<ObstacleSlope> obstacles, List<AbstractSyncPhysical> physicalAreas) {
        ShareTestGuiDisplay.show(new AbstractTestGuiRenderer() {
            @Override
            protected void doRender() {
                if (obstacles != null) {
                    obstacles.forEach(obstacleSlope -> strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1)));
                }
                if (physicalAreas != null) {
                    physicalAreas.forEach(physicalArea -> {
                        if (physicalArea instanceof SyncPhysicalMovable) {
                            strokeSyncPhysicalMovable((SyncPhysicalMovable) physicalArea, 0.2, Color.GREEN);
                        } else {
                            strokeSyncPhysicalArea(physicalArea, 0.2, Color.GRAY);
                        }
                    });
                }
                orca.getDebugObstacles_WRONG().forEach(obstacleSlope -> strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8)));
                strokeSyncPhysicalMovable(protagonist, 0.2, Color.GREEN);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                    strokeLine(new Line(protagonist.getPosition(), protagonist.getPosition().add(orca.getNewVelocity())), 0.2, Color.DARKBLUE);
                    System.out.println("Speed: " + orca.getNewVelocity().length() + " Velocity: " + orca.getNewVelocity());
                }

                // System.out.println("orcaLines: " + orca.getOrcaLines().size());
                for (OrcaLine orcaLine : orca.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }
            }
        });
    }
}