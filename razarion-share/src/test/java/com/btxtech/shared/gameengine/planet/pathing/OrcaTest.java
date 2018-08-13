package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.TestHelper;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.TestGuiDisplay;
import com.btxtech.shared.system.debugtool.DebugHelperStatic;
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
    //    private static final DecimalPosition POINT_TRIANGLE_1 = new DecimalPosition(20, 10);
//    private static final DecimalPosition POINT_TRIANGLE_2 = new DecimalPosition(30, 20);
//    private static final DecimalPosition POINT_TRIANGLE_3 = new DecimalPosition(20, 30);
    private static final DecimalPosition POINT_TRIANGLE_1 = new DecimalPosition(10, 20);
    private static final DecimalPosition POINT_TRIANGLE_2 = new DecimalPosition(20, 10);
    private static final DecimalPosition POINT_TRIANGLE_3 = new DecimalPosition(30, 20);
    // private SyncPhysicalMovable syncPhysicalMovable1;
    // private Orca orca1;

    @Test
    public void testGui() {
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(78.25, 160.0), new DecimalPosition(-15.0, 1.83697019872103E-15));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(61.749999999999986, 158.0), new DecimalPosition(14.97953701566793, 0.7832437655251202));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(61.749999999999986, 162.0), new DecimalPosition(14.97953701566793, -0.7832437655251202));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(27.01204246464725, 20.0), new DecimalPosition(-12.879575353527443, 1.5772930731074524E-15));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(13.24128433005918, 17.83853468818384), new DecimalPosition(15.473272819270125, 0.9098506802964705));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(13.24128433005918, 22.16146531181616), new DecimalPosition(15.473272819270127, -0.9098506802964706));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(73.54347670132562, 160.0), new DecimalPosition(-1.0326164933719664, 1.2645904833547426E-16));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(69.67934615779934, 157.36264913700253), new DecimalPosition(16.936052765691972, 1.4731316023007792));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(69.67934615779936, 162.63735086299747), new DecimalPosition(16.936052765691972, -1.4731316023007799));
        ///
//        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(23.54347670132562, 20.0), new DecimalPosition(-1.0326164933719664, 1.2645904833547426E-16));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(19.67934615779934, 17.36264913700253), new DecimalPosition(16.936052765691972, 1.4731316023007792));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(19.67934615779936, 22.63735086299747), new DecimalPosition(16.936052765691972, -1.4731316023007799));
        //
        // SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(83.750, 60.000), new DecimalPosition(-12.500, 0.000), new DecimalPosition(-13.000, 0.000));


//        ObstacleSlope obstacleSlope1 = new ObstacleSlope(POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_3);
//        ObstacleSlope obstacleSlope2 = new ObstacleSlope(POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_1);
//        ObstacleSlope obstacleSlope3 = new ObstacleSlope(POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_2);

        DebugHelperStatic.setCurrentTick(1);
        // orca1.add(syncPhysicalMovable2);
        // orca1.add(syncPhysicalMovable3);
//        System.out.println("orca1: " + orca1.getNewVelocity() + ". speed: " + orca1.getNewVelocity().magnitude() + ". angle speed: " + Math.toDegrees(orca1.getNewVelocity().angle()));
//            Orca orca2 = new Orca(syncPhysicalMovable2);
//            orca2.add(syncPhysicalMovable1);
//            orca2.add(syncPhysicalMovable3);
//            orca2.solve();
//            System.out.println("orca2: " + orca2.getNewVelocity() + ". speed: " + orca2.getNewVelocity().magnitude() + ". angle speed: " + Math.toDegrees(orca2.getNewVelocity().angle()));
//            Orca orca3 = new Orca(syncPhysicalMovable3);
//            orca3.add(syncPhysicalMovable1);
//            orca3.add(syncPhysicalMovable2);
//            orca3.solve();
//            System.out.println("orca3: " + orca3.getNewVelocity());
        DebugHelperStatic.printAfterTick(null);

        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            private DecimalPosition position = new DecimalPosition(243.07001332001306, 339.65095460095426);

            @Override
            protected void doRender() {
                List<ObstacleSlope> obstacles = new ArrayList<>();
                // obstacles.addAll(createObstacleSlopes(new DecimalPosition(20, 40), new DecimalPosition(20, 20), new DecimalPosition(40, 40)));
                // ------------- Written code -------------
                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, new DecimalPosition(3.847037675993762, 16.558994568496136), new DecimalPosition(3.8470376759936222, 16.558994568496168), 17.0);
                obstacles.addAll(new ArrayList<>(createObstacleSlopes(
                        new DecimalPosition(105.278024733339, 104.042797975819),
                        new DecimalPosition(106.1484457143097, 101.3639176522738),
                        new DecimalPosition(110.9037282957856, 102.9090026241486),
                        new DecimalPosition(110.033307314815, 105.5878829476937),
                        new DecimalPosition(104.4076037523687, 106.721678299364)
                )));
                // ------------- Written code ends -------------
                // ------------- Generated code -------------
                // SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(2879.031, 1436.997), new DecimalPosition(0.000, -0.053), new DecimalPosition(-0.550, 0.060), 17.0);
                // obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(2873.495, 1435.028), new DecimalPosition(2877.030, 1438.564), new DecimalPosition(0.707, -0.707), true, new DecimalPosition(0.707, 0.707), true, new DecimalPosition(-0.707, 0.707)));
                // ------------- Generated code ends -------------
                Orca orca = new Orca(syncPhysicalMovable1);
                ObstacleSlope.sort(syncPhysicalMovable1.getPosition2d(), obstacles);
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                orca.getDebugObstacles_WRONG().forEach(obstacleSlope -> {
                    strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8));
                });
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.05, Color.RED);
//                strokeSyncPhysicalMovable(syncPhysicalMovable9, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable5, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable15, 0.05, Color.GREEN);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                    // System.out.println("New Velocity: " + orca.getNewVelocity() + ". speed: " + orca.getNewVelocity().magnitude());
                }

                // System.out.println("orcaLines: " + orca.getOrcaLines().size());
                for (OrcaLine orcaLine : orca.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
//                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, position, new DecimalPosition(12.500, 0.000), new DecimalPosition(13.000, 0.000));
//                // syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(17.5, 25.076923076923077), new DecimalPosition(12.500, 0.000), new DecimalPosition(13.000, 0.000));
//                orca1 = new Orca(syncPhysicalMovable1);
//                orca1.add(obstacleSlope);
//                orca1.solve();
                this.position = position;
                return true;
            }

            @Override
            protected void onMousePressedTerrain(DecimalPosition position) {
                System.out.println("**** " + position.getX() + ", " + position.getY());
            }

            @Override
            protected void onGenTestButtonClicked(DecimalPosition mousePosition) {
                System.out.println("Mouse position: " + mousePosition.testString());
            }
        });
    }

    @Test
    public void testSlope1() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
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
        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        // display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    @Test
    public void testSlope2() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40, 40), new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
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
        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    @Test
    public void testSlope3() {
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(109.327796766943, 87.280505573016), new DecimalPosition(3.847037675993762, 16.558994568496136), new DecimalPosition(3.8470376759936222, 16.558994568496168), 17.0);
        Orca orca = new Orca(syncPhysicalMovable);
        List<ObstacleSlope> obstacles = new ArrayList<>(createObstacleSlopes(
                new DecimalPosition(105.278024733339, 104.042797975819),
                new DecimalPosition(106.1484457143097, 101.3639176522738),
                new DecimalPosition(110.9037282957856, 102.9090026241486),
                new DecimalPosition(110.033307314815, 105.5878829476937),
                new DecimalPosition(104.4076037523687, 106.721678299364)
        ));

        ObstacleSlope.sort(syncPhysicalMovable.getPosition2d(), obstacles);
        obstacles.forEach(orca::add);
        orca.solve();
        display(orca, syncPhysicalMovable, obstacles);
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352, 11.98284), orca.getNewVelocity());
    }

    private void display(Orca orca, SyncPhysicalMovable syncPhysicalMovable, List<ObstacleSlope> obstacles) {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            @Override
            protected void doRender() {
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                orca.getDebugObstacles_WRONG().forEach(obstacleSlope -> {
                    strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8));
                });
                strokeSyncPhysicalMovable(syncPhysicalMovable, 0.05, Color.RED);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                }

                // System.out.println("orcaLines: " + orca.getOrcaLines().size());
                for (OrcaLine orcaLine : orca.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }
            }

            @Override
            protected void onMousePressedTerrain(DecimalPosition position) {
                System.out.println("new DecimalPosition(" + position.getX() + ", " + position.getY() + "),");
            }
        });
    }
}