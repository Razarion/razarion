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
import com.btxtech.shared.utils.CollectionUtils;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
        DebugHelperStatic.printAfterTick();

        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            private DecimalPosition position = new DecimalPosition(29.9, 22);

            @Override
            protected void doRender() {
                List<ObstacleSlope> obstacles = new ArrayList<>();
                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
                Orca orca = new Orca(syncPhysicalMovable1);
                // obstacles.addAll(createObstacleSlopes(new DecimalPosition(20, 40), new DecimalPosition(20, 20), new DecimalPosition(40, 40)));
                obstacles.addAll(createObstacleSlopes(
                        new DecimalPosition(20, 20),
                        new DecimalPosition(60, 20),
                        new DecimalPosition(60, 60),
                        new DecimalPosition(100, 60),
                        new DecimalPosition(100, 65),
                        new DecimalPosition(55, 65),
                        new DecimalPosition(55, 25),
                        new DecimalPosition(20, 25)
                ));

//                obstacles.add(obstacleSlope1);
//                obstacles.add(obstacleSlope2);
//                obstacles.add(obstacleSlope3);
                // ------------- Written code -------------

                // ------------- Written code ends -------------
                // ------------- Generated code -------------
//                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(240.827, 340.472), new DecimalPosition(10.877, 12.992), new DecimalPosition(9.929, 13.799), 17.0);
//                Orca orca = new Orca(syncPhysicalMovable1);
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(236.110, 341.873), new DecimalPosition(239.345, 345.622), new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757)));
//
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(240.960, 359.396), new DecimalPosition(240.935, 358.493), new DecimalPosition(0.414, -0.910), false, new DecimalPosition(-0.029, -1.000), false, new DecimalPosition(-0.465, -0.885)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(240.587, 360.218), new DecimalPosition(240.960, 359.396), new DecimalPosition(0.772, -0.635), false, new DecimalPosition(0.414, -0.910), false, new DecimalPosition(-0.029, -1.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(229.641, 334.374), new DecimalPosition(232.876, 338.124), new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(242.580, 349.372), new DecimalPosition(245.814, 353.121), new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.465, 0.885)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(230.810, 346.446), new DecimalPosition(227.576, 342.696), new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(237.280, 353.944), new DecimalPosition(234.045, 350.195), new DecimalPosition(-0.653, -0.757), false, new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(239.345, 345.622), new DecimalPosition(242.580, 349.372), new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(234.045, 350.195), new DecimalPosition(230.810, 346.446), new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(245.814, 353.121), new DecimalPosition(247.706, 356.718), new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.465, 0.885), true, new DecimalPosition(0.029, 1.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(223.172, 326.876), new DecimalPosition(226.406, 330.625), new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(240.935, 358.493), new DecimalPosition(240.514, 357.694), new DecimalPosition(-0.029, -1.000), false, new DecimalPosition(-0.465, -0.885), false, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(232.876, 338.124), new DecimalPosition(236.110, 341.873), new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(224.341, 338.947), new DecimalPosition(221.106, 335.198), new DecimalPosition(-0.653, -0.757), false, new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(227.576, 342.696), new DecimalPosition(224.341, 338.947), new DecimalPosition(-0.653, -0.757), true, new DecimalPosition(-0.653, -0.757), false, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(247.706, 356.718), new DecimalPosition(247.822, 360.780), new DecimalPosition(0.465, 0.885), true, new DecimalPosition(0.029, 1.000), true, new DecimalPosition(-0.414, 0.910)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(240.514, 357.694), new DecimalPosition(237.280, 353.944), new DecimalPosition(-0.465, -0.885), false, new DecimalPosition(-0.653, -0.757), false, new DecimalPosition(-0.653, -0.757)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(226.406, 330.625), new DecimalPosition(229.641, 334.374), new DecimalPosition(0.653, 0.757), false, new DecimalPosition(0.653, 0.757), true, new DecimalPosition(0.653, 0.757)));
                // ------------- Generated code ends -------------
                ObstacleSlope.sort(syncPhysicalMovable1.getPosition2d(), obstacles);
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                orca.getDebugObstacles().forEach(obstacleSlope -> {
                    strokeObstacleSlope(obstacleSlope, 0.4, new Color(1, 1, 0, 0.8));
                });
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.05, Color.RED);
//                strokeSyncPhysicalMovable(syncPhysicalMovable9, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable5, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable15, 0.05, Color.GREEN);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
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
        SyncPhysicalMovable syncPhysicalMovable = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(40,40), new DecimalPosition(10, 10), new DecimalPosition(10, 10), 17.0);
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
        TestHelper.assertDecimalPosition("New velocity wrong", new DecimalPosition(6.11352,11.98284), orca.getNewVelocity());
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
                orca.getDebugObstacles().forEach(obstacleSlope -> {
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
        });
    }
}