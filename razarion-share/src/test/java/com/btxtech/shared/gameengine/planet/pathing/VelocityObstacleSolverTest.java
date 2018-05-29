package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.GameTestHelper;
import com.btxtech.shared.gameengine.planet.PlanetService;
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
public class VelocityObstacleSolverTest {
    @Test
    public void test() {
        TestGuiDisplay.show(new AbstractTestGuiRenderer() {
            VelocityObstacleSolver velocityObstacleSolver1;
            VelocityObstacleSolver velocityObstacleSolver2;
            SyncPhysicalMovable syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(20, 20), new DecimalPosition(30, 0));
            SyncPhysicalMovable syncPhysicalMovable2/* = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(60, 50), new DecimalPosition(17, 0))*/;

            @Override
            protected void doRender() {
//                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(47.788119, 50.891979), new DecimalPosition(16.967255,-1.054636));
//                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(50.891979, 47.788119), new DecimalPosition(-1.054636,16.967255));

//                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(54.77921535706048, 58.3792153570605), new DecimalPosition(16.132926282949995, -5.359915069187317));
//                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(58.3792153570605, 54.77921535706048), new DecimalPosition(-5.359915069187318, 16.132926282949995));

                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(66.39999999999999, 10), new DecimalPosition(16.5, 0));
                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(73.60000000000001, 10), new DecimalPosition(-16.5, 2.0206672185931327E-15));

                strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.1, Color.RED);
                strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.RED);
                strokeLine(new Line(DecimalPosition.NULL, syncPhysicalMovable1.getVelocity().multiply(PlanetService.TICK_FACTOR)), 0.02, Color.YELLOWGREEN);

                velocityObstacleSolver1 = new VelocityObstacleSolver(syncPhysicalMovable1);
                velocityObstacleSolver1.analyzeAndAdd(syncPhysicalMovable2);
                velocityObstacleSolver1.solve();
                velocityObstacleSolver2 = new VelocityObstacleSolver(syncPhysicalMovable2);
                velocityObstacleSolver2.analyzeAndAdd(syncPhysicalMovable1);
                velocityObstacleSolver2.solve();

                if (velocityObstacleSolver1 != null) {
                    velocityObstacleSolver1.implementVelocity();
                    velocityObstacleSolver2.implementVelocity();
                    syncPhysicalMovable1.implementPosition();
                    syncPhysicalMovable2.implementPosition();
                    strokeSyncPhysicalMovable(syncPhysicalMovable1, 0.1, Color.GREEN);
                    strokeSyncPhysicalMovable(syncPhysicalMovable2, 0.1, Color.GREEN);
                    if (velocityObstacleSolver1.getReciprocalVelocityObstacles() != null && !velocityObstacleSolver1.getReciprocalVelocityObstacles().isEmpty()) {
                        ReciprocalVelocityObstacle reciprocalVelocityObstacle = velocityObstacleSolver1.getReciprocalVelocityObstacles().stream().findFirst().get();
                        strokeLine(reciprocalVelocityObstacle.getFlank1(), 0.1, Color.RED);
                        strokeLine(reciprocalVelocityObstacle.getFlank2(), 0.1, Color.RED);
                        strokeLine(reciprocalVelocityObstacle.getMiddle(), 0.1, Color.GREEN);
                        if (velocityObstacleSolver1.getBestVelocity() != null) {
                            strokeDecimalPosition(velocityObstacleSolver1.getBestVelocity().multiply(PlanetService.TICK_FACTOR), 0.5, Color.ORANGE);
                        }
                    }
                }
            }

            @Override
            protected boolean onMouseMoved(DecimalPosition position) {
                //syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, position, new DecimalPosition(20, 10));
                syncPhysicalMovable1 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(20, 18), new DecimalPosition(20, 10));
                syncPhysicalMovable2 = GameTestHelper.createSyncPhysicalMovable(2, TerrainType.LAND, new DecimalPosition(20, 22), new DecimalPosition(20, -10));
                velocityObstacleSolver1 = new VelocityObstacleSolver(syncPhysicalMovable1);
                velocityObstacleSolver1.analyzeAndAdd(syncPhysicalMovable2);
                velocityObstacleSolver1.solve();
                velocityObstacleSolver2 = new VelocityObstacleSolver(syncPhysicalMovable2);
                velocityObstacleSolver2.analyzeAndAdd(syncPhysicalMovable1);
                velocityObstacleSolver2.solve();
                return true;
            }
        });
    }
}