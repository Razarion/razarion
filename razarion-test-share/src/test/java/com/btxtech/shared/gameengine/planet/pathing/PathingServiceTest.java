package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.paint.Color;
import org.junit.Test;

public class PathingServiceTest extends AStarBaseTest {
    @Test
    public void testCaseGenerator() {
        double radius = 2;
        DecimalPosition builder = new DecimalPosition(150, 150);

        showDisplay(new MouseMoveCallback().setCallback(position -> {
            try {
                DecimalPosition nearestPosition = getPathingService().findNearestPosition(position,
                        TerrainType.LAND,
                        builder,
                        radius);

                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(builder, radius), Color.PINK).addCircleColor(new Circle2D(nearestPosition, 1), Color.RED)};
            } catch (Exception e) {
                System.out.println(e.getMessage());
                // e.printStackTrace();
                return new Object[]{new PositionMarker().addCircleColor(new Circle2D(position, 1), Color.PLUM)};
            }
        }));
    }

}
