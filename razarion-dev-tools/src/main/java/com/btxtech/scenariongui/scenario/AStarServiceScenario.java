package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.model.DevToolHelper;
import com.btxtech.shared.gameengine.planet.pathing.PathingService;

/**
 * Created by Beat
 * 19.03.2016.
 */
public class AStarServiceScenario extends AbstractTerrainScenario {
    private DecimalPosition start;
    private SimplePath path;

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
//
//        if (path != null) {
//            extendedGraphicsContext.strokeCurveDecimalPosition(path.getWayPositions(), 0.1, Color.BLUE, true);
//        }
//
//        if (start != null) {
//            extendedGraphicsContext.drawPosition(start, 0.5, Color.BLACK);
//        }
    }

    @Override
    public boolean onMouseDown(DecimalPosition position) {
        if (start != null && path != null) {
            start = null;
            path = null;
        } else if (start == null) {
            start = position;
        } else {
            PathingService pathingService = getBean(PathingService.class);
            path = pathingService.setupPathToDestination(DevToolHelper.generateSyncBaseItem(start), position);
        }
        return true;
    }
}
