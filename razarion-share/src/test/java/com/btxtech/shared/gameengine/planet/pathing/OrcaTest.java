package com.btxtech.shared.gameengine.planet.pathing;

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


        ObstacleSlope obstacleSlope1 = new ObstacleSlope(POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_3);
        ObstacleSlope obstacleSlope2 = new ObstacleSlope(POINT_TRIANGLE_2, POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_1);
        ObstacleSlope obstacleSlope3 = new ObstacleSlope(POINT_TRIANGLE_3, POINT_TRIANGLE_1, POINT_TRIANGLE_2, POINT_TRIANGLE_2);

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
                SyncPhysicalMovable syncPhysicalMovable16 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(178.136, 101.101), new DecimalPosition(9.399, -6.811), new DecimalPosition(11.792, 2.743), 17.0);
                Orca orca = new Orca(syncPhysicalMovable16);
                SyncPhysicalMovable syncPhysicalMovable12 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(170.145, 107.301), new DecimalPosition(9.334, -6.597), new DecimalPosition(10.891, -4.870), 17.0);
                orca.add(syncPhysicalMovable12);
//                SyncPhysicalMovable syncPhysicalMovable17 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(174.228, 108.092), new DecimalPosition(9.317, -6.795), new DecimalPosition(9.469, -7.422), 17.0);
//                orca.add(syncPhysicalMovable17);
//                SyncPhysicalMovable syncPhysicalMovable18 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(172.439, 112.146), new DecimalPosition(9.366, -6.779), new DecimalPosition(8.272, -8.778), 17.0);
//                orca.add(syncPhysicalMovable18);
//                SyncPhysicalMovable syncPhysicalMovable21 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(184.860, 91.316), new DecimalPosition(-15.444, -7.106), new DecimalPosition(-15.444, -7.106), 17.0);
//                orca.add(syncPhysicalMovable21);
//                SyncPhysicalMovable syncPhysicalMovable22 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(189.557, 94.958), new DecimalPosition(12.108, -11.933), new DecimalPosition(-14.214, -9.325), 17.0);
//                orca.add(syncPhysicalMovable22);
//                SyncPhysicalMovable syncPhysicalMovable23 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(185.351, 97.964), new DecimalPosition(8.380, -14.791), new DecimalPosition(-6.593, -15.670), 17.0);
//                orca.add(syncPhysicalMovable23);
//                SyncPhysicalMovable syncPhysicalMovable24 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(182.029, 102.155), new DecimalPosition(11.583, -12.443), new DecimalPosition(-0.041, -17.000), 17.0);
//                orca.add(syncPhysicalMovable24);
//                SyncPhysicalMovable syncPhysicalMovable25 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(179.398, 107.809), new DecimalPosition(12.118, -11.923), new DecimalPosition(6.950, -15.514), 17.0);
//                orca.add(syncPhysicalMovable25);
//                SyncPhysicalMovable syncPhysicalMovable11 = GameTestHelper.createSyncPhysicalMovable(2.0, TerrainType.LAND, new DecimalPosition(176.185, 104.604), new DecimalPosition(10.394, -6.192), new DecimalPosition(11.499, -5.148), 17.0);
//                orca.add(syncPhysicalMovable11);
                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(179.687, 99.000), new DecimalPosition(174.781, 99.000), new DecimalPosition(179.687, 92.000), new DecimalPosition(169.875, 99.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(174.781, 92.000), new DecimalPosition(179.687, 92.000), new DecimalPosition(169.875, 92.000), new DecimalPosition(174.781, 58.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(179.687, 92.000), new DecimalPosition(179.687, 99.000), new DecimalPosition(179.687, 51.000), new DecimalPosition(174.781, 99.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(164.968, 92.000), new DecimalPosition(169.875, 92.000), new DecimalPosition(160.062, 92.000), new DecimalPosition(174.781, 92.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(174.781, 99.000), new DecimalPosition(169.875, 99.000), new DecimalPosition(179.687, 99.000), new DecimalPosition(164.968, 99.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(164.968, 99.000), new DecimalPosition(160.062, 99.000), new DecimalPosition(169.875, 99.000), new DecimalPosition(155.156, 99.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(169.875, 92.000), new DecimalPosition(174.781, 92.000), new DecimalPosition(164.968, 92.000), new DecimalPosition(179.687, 92.000)));
//                obstacles.add(GameTestHelper.createObstacleSlope(new DecimalPosition(169.875, 99.000), new DecimalPosition(164.968, 99.000), new DecimalPosition(174.781, 99.000), new DecimalPosition(160.062, 99.000)));
                // ------------- Generated code ends -------------
                obstacles.forEach(obstacleSlope -> {
                    orca.add(obstacleSlope);
                    strokeObstacleSlope(obstacleSlope, 0.2, new Color(0, 0, 0, 0.1));
                });
                orca.solve();
                strokeSyncPhysicalMovable(syncPhysicalMovable16, 0.05, Color.RED);


                if (!orca.getNewVelocity().equalsDeltaZero()) {
                    strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.2, Color.DARKBLUE);
                }

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