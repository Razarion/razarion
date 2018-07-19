package com.btxtech.shared.gameengine.planet.pathing;

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
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
//                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, position, new DecimalPosition(10, -10), new DecimalPosition(10, -10), 17.0);
//                Orca orca = new Orca(syncPhysicalMovable1);
//                obstacles.add(obstacleSlope1);
//                obstacles.add(obstacleSlope2);
//                obstacles.add(obstacleSlope3);
                // ------------- Generated code -------------
                SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(125.972, 89.836), new DecimalPosition(-16.999, 0.139), new DecimalPosition(-16.999, 0.139), 17.0);
                Orca orca = new Orca(syncPhysicalMovable1);

                List<DecimalPosition> polygon = Arrays.asList(new DecimalPosition(110.000, 99.268), new DecimalPosition(109.268, 110.000), new DecimalPosition(104, 104));
                for (int i = 0; i < polygon.size(); i++) {
                    DecimalPosition previous = CollectionUtils.getCorrectedElement(i - 1, polygon);
                    DecimalPosition point1 = polygon.get(i);
                    DecimalPosition point2 = CollectionUtils.getCorrectedElement(i + 1, polygon);
                    DecimalPosition next = CollectionUtils.getCorrectedElement(i + 2, polygon);
                    if (point1.equals(point2)) {
                        // also for previous and next ???
                        continue;
                    }
                    obstacles.add(new ObstacleSlope(point1, point2, previous, next));
                }


                // obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(110.000, 99.268), new DecimalPosition(109.268, 110.000), false, new DecimalPosition(-0.707, 0.707), false, new DecimalPosition(-0.259, 0.966)));
                // ------------- Generated code ends -------------
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0.5, 0.1));
                });
                orca.solve();
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.05, Color.RED);
//                strokeSyncPhysicalMovable(syncPhysicalMovable9, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable5, 0.05, Color.GREEN);
//                strokeSyncPhysicalMovable(syncPhysicalMovable15, 0.05, Color.GREEN);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                }

                System.out.println("orcaLines: " + orca.getOrcaLines().size());
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
            protected void onGenTestButtonClicked(DecimalPosition mousePosition) {
                System.out.println("Mouse position: " + mousePosition.testString());
            }
        });
    }
}