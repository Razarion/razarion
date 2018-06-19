package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Line;
import com.btxtech.shared.gameengine.planet.model.SyncPhysicalMovable;
import com.btxtech.shared.gameengine.planet.pathing.ClearanceHole;
import com.btxtech.shared.gameengine.planet.pathing.ObstacleSlope;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Beat
 * 05.04.2017.
 */
@Deprecated
public class ClearanceHoleScenario extends Scenario {
    private DecimalPosition target;

    @Override
    public void init() {
    }

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        SyncPhysicalMovable syncPhysicalMovable = FrameworkHelper.createSyncPhysicalMovable(new DecimalPosition(0, 0), 2);
        // SyncPhysicalMovable other = FrameworkHelper.createSyncPhysicalMovable(new DecimalPosition(5, 0), 2);
        Collection<ObstacleSlope> obstacleSlopes = new ArrayList<>();
        // obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(-2, -9), new DecimalPosition(3, -9))));
        //obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(4, 4), new DecimalPosition(4, 7))));
//        obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(4, 7), new DecimalPosition(4, 10))));
//        obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(4, 10), new DecimalPosition(4, 13))));
//        obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(4, 13), new DecimalPosition(5, 14))));
        obstacleSlopes.add(new ObstacleSlope(new Line(new DecimalPosition(2, 0), new DecimalPosition(2, 3))));

        extendedGraphicsContext.drawSyncPhysicalArea(syncPhysicalMovable, Color.GREEN);
        // extendedGraphicsContext.drawSyncPhysicalArea(other, Color.RED);
        obstacleSlopes.forEach(obstacleSlope -> extendedGraphicsContext.drawObstacle(obstacleSlope, Color.BLUE, Color.BLUE));

        ClearanceHole clearanceHole = new ClearanceHole(syncPhysicalMovable);
        // clearanceHole.addOther(other);
        obstacleSlopes.forEach(clearanceHole::addOther);
        if (target != null) {
            double angle = clearanceHole.getFreeAngle(syncPhysicalMovable.getDesiredPosition().getAngle(target));
            DecimalPosition directionLine = syncPhysicalMovable.getPosition2d().getPointWithDistance(angle, 20);
            extendedGraphicsContext.getGc().strokeLine(syncPhysicalMovable.getPosition2d().getX(), syncPhysicalMovable.getPosition2d().getY(), directionLine.getX(), directionLine.getY());
        }
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        target = position;
        return true;
    }
}
