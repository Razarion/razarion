package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
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
public class ReciprocalVelocityObstacleTest {
    @Test
    public void test() {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(30, 0));
            SyncPhysicalMovable syncPhysicalMovable2/* = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(60, 50), new DecimalPosition(17, 0))*/;
            Orca orca/* = new ReciprocalVelocityObstacle(syncPhysicalMovable1, syncPhysicalMovable2)*/;

            @Override
            protected void doRender() {
                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.1, Color.RED);
                if (syncPhysicalMovable2 != null) {
                    strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.RED);
                    // strokeLine(new Line(DecimalPosition.NULL, syncPhysicalMovable2.getVelocity().multiply(PlanetService.TICK_FACTOR)), Color.YELLOWGREEN, 0.2);
                    if (orca.getU() != null) {
                        strokeCircle(new Circle2D(orca.getRelativePosition(), orca.getCombinedRadius()), 0.2, Color.BROWN);
                        strokeCircle(new Circle2D(orca.getRelativePosition().divide(Orca.TAU), orca.getCombinedRadius() / Orca.TAU), 0.2, Color.SANDYBROWN);
                        strokeDecimalPosition(orca.getRelativeVelocity().add(orca.getU()), 0.2, Color.RED);
                        strokeLine(orca.getLine(), 0.1, Color.ORANGE);
                        strokeLine(orca.getLine1(), 0.1, Color.GREEN);
                    }
                    // strokeLine(orca.getFlank2(), Color.RED, 0.1);

                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, position, new DecimalPosition(-30, 0));
                orca = new Orca(syncPhysicalMovable1, syncPhysicalMovable2);
                return true;
            }
        });
    }

}