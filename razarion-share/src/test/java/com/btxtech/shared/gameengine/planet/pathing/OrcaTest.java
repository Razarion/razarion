package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import com.btxtech.shared.gui.AbstractTestGuiRenderer;
import com.btxtech.shared.gui.TestGuiDisplay;
import javafx.scene.paint.Color;
import org.junit.Test;

/**
 * Created by Beat
 * on 24.05.2018.
 */
public class OrcaTest {
    @Test
    public void frontal1() {
        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(30, 0));
        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(24.5, 20), new DecimalPosition(-30, 0));
        //Orca orca12 = new Orca(syncPhysicalMovable1, syncPhysicalMovable2);
        //System.out.println("New Velocity 1: " + orca12.getNewVelocity() + " magnitude: " + orca12.getNewVelocity().magnitude());
    }


    @Test
    public void testGui() {
        SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(91.97428218141735, 160.0), new DecimalPosition(16.816433637282703, 0.000000));

        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(95.85000000000004, 158.0), new DecimalPosition(15.314356296497165, 7.380412673010746));
        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(95.85000000000004, 162.0), new DecimalPosition(15.314356296497165, -7.380412673010746));
//        SyncPhysicalMovable syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(95.85000000000004, 158.0), new DecimalPosition(17, 0));
//        SyncPhysicalMovable syncPhysicalMovable3 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(95.85000000000004, 162.0), new DecimalPosition(17, 0));
        Orca orca1 = new Orca(syncPhysicalMovable1);
        orca1.add(syncPhysicalMovable2);
        orca1.add(syncPhysicalMovable3);
        orca1.solve();
        System.out.println("orca1: " + orca1.getNewVelocity() + ". speed: " + orca1.getNewVelocity().magnitude());
//            Orca orca2 = new Orca(syncPhysicalMovable2);
//            orca2.add(syncPhysicalMovable1);
//            orca2.add(syncPhysicalMovable3);
//            orca2.solve();
//            System.out.println("orca2: " + orca2.getNewVelocity());
//            Orca orca3 = new Orca(syncPhysicalMovable3);
//            orca3.add(syncPhysicalMovable1);
//            orca3.add(syncPhysicalMovable2);
//            orca3.solve();
//            System.out.println("orca3: " + orca3.getNewVelocity());

        TestGuiDisplay.show(new AbstractTestGuiRenderer() {

            @Override
            protected void doRender() {
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.1, Color.RED);
                strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.GREEN);
                strokeSyncPhysicalMovable(syncPhysicalMovable3, 0.1, Color.GREEN);

                for (OrcaLine orcaLine : orca1.getOrcaLines()) {
                    strokeLine(orcaLine.toLine(), 0.1, Color.ORANGE);
                }
                strokeLine(new Line(DecimalPosition.NULL, orca1.getNewVelocity()), 0.1, Color.POWDERBLUE);


//                if (syncPhysicalMovable2 != null) {
//                    strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.RED);
//                    // strokeLine(new Line(DecimalPosition.NULL, syncPhysicalMovable2.getVelocity().multiply(PlanetService.TICK_FACTOR)), Color.YELLOWGREEN, 0.2);
////                    if (orca.getU() != null) {
////                        strokeCircle(new Circle2D(orca.getRelativePosition(), orca.getCombinedRadius()), 0.2, Color.BROWN);
////                        strokeCircle(new Circle2D(orca.getRelativePosition().divide(Orca.TAU), orca.getCombinedRadius() / Orca.TAU), 0.2, Color.SANDYBROWN);
////                        strokeDecimalPosition(orca.getRelativeVelocity().add(orca.getU()), 0.2, Color.RED);
////                        strokeLine(orca.getLine().toLine(), 0.1, Color.ORANGE);
////                        // strokeLine(new Line(DecimalPosition.NULL, orca.getRelativeVelocity()), 0.1, Color.BLACK);
////                        if (orca.getNewVelocity() != null) {
////                            strokeLine(new Line(DecimalPosition.NULL, orca.getNewVelocity()), 0.1, Color.POWDERBLUE);
////                        }
////                    }
//                    // strokeLine(orca.getFlank2(), Color.RED, 0.1);
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