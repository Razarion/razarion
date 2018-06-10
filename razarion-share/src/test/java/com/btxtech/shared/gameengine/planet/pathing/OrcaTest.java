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

/**
 * Created by Beat
 * on 24.05.2018.
 */
public class OrcaTest {

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
        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(56.250, 160.000), new DecimalPosition(12.500, 0.000), new DecimalPosition(13.000, 0.000));
        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(83.750, 160.000), new DecimalPosition(-12.500, 0.000), new DecimalPosition(-13.000, 0.000));


        DebugHelperStatic.setCurrentTick(1);
        Orca orca1 = new Orca(syncPhysicalMovable1);
        orca1.add(syncPhysicalMovable2);
        // orca1.add(syncPhysicalMovable3);
        orca1.solve();
        System.out.println("orca1: " + orca1.getNewVelocity() + ". speed: " + orca1.getNewVelocity().magnitude() + ". angle speed: " + Math.toDegrees(orca1.getNewVelocity().angle()));
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

            @Override
            protected void doRender() {
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.05, Color.RED);
                strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.05, Color.GREEN);
                // strokeSyncPhysicalMovable(syncPhysicalMovable3, 0.05, Color.GREEN);

                for (OrcaLine orcaLine : orca1.getOrcaLines()) {
                    strokeOrcaLine(orcaLine);
                }

                //     strokeOrcaLine(orca1.getOrcaLines().get(0));
                strokeLine(new Line(DecimalPosition.NULL, orca1.getNewVelocity()), 0.1, Color.POWDERBLUE);


//                if (syncPhysicalMovable2 != null) {
//                    strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.RED);
//                    // strokeLine(new Line(DecimalPosition.NULL, syncPhysicalMovable2.getVelocity().multiply(PlanetService.TICK_FACTOR)), Color.YELLOWGREEN, 0.2);
//
//                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
//                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, position, new DecimalPosition(-30, 0));
////                 orca = new Orca(syncPhysicalMovable1, syncPhysicalMovable2);
//                System.out.println("New Velocity: " + orca.getNewVelocity() + " magnitude: " + orca.getNewVelocity().magnitude());
                return true;
            }

            @Override
            protected void onGenTestButtonClicked(DecimalPosition mousePosition) {
                System.out.println("Mouse position: " + mousePosition);
            }
        });
    }
}