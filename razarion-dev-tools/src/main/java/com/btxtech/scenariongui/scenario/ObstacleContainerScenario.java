package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;

/**
 * Created by Beat
 * 22.01.2017.
 */
public class ObstacleContainerScenario extends AbstractTerrainScenario {
    private DecimalPosition mousePosition;

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
//        ObstacleContainer obstacleContainer = getBean(ObstacleContainer.class);
//
//        // Draw ObstacleContainer
//        for (int x = 0; x < obstacleContainer.getXCount(); x++) {
//            for (int y = 0; y < obstacleContainer.getYCount(); y++) {
//                Index index = new Index(x, y);
//                if (obstacleContainer.getObstacleContainerNode(index) != null) {
//                    DecimalPosition absolutePosition = obstacleContainer.toAbsolute(index);
//                    extendedGraphicsContext.getGc().setFill(Color.GREEN);
//                    extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH);
//                    extendedGraphicsContext.getGc().setFill(Color.RED);
//                    extendedGraphicsContext.getGc().fillRect(absolutePosition.getX(), absolutePosition.getY(), TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1, TerrainUtil.GROUND_NODE_ABSOLUTE_LENGTH - 0.1);
//                }
//            }
//        }

        // Draw terrain
//        for (Slope slope : getTerrainService().getSlopes()) {
//            extendedGraphicsContext.strokeTriangles(slope.getMesh().getVertices(), 0.05, Color.BLUE);
//        }

        drawObstacle(extendedGraphicsContext);

//        if (mousePosition != null) {
//            double radius = 10;
//            extendedGraphicsContext.getGc().setFill(Color.BLUE);
//            extendedGraphicsContext.getGc().fillOval(mousePosition.getX() - radius, mousePosition.getY() - radius, radius * 2.0, radius * 2.0);
//            SyncPhysicalArea syncPhysicalArea = new SyncPhysicalArea();
//            syncPhysicalArea.init(null, radius, false, mousePosition, 0);
//            for (Obstacle obstacle : obstacleContainer.getObstacles(syncPhysicalArea)) {
//                extendedGraphicsContext.drawObstacle(obstacle, new Color(0, 0, 0, 0.5), new Color(0, 0, 0, 0.5));
//            }
//        }

    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        mousePosition = position;
        return true;
    }
}
