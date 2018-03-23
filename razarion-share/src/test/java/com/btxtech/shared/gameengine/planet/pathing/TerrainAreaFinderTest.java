package com.btxtech.shared.gameengine.planet.pathing;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.gui.userobject.MouseMoveCallback;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.terrain.container.TerrainType;
import javafx.scene.paint.Color;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Beat
 * on 22.03.2018.
 */
public class TerrainAreaFinderTest extends AStarBaseTest {

    @Test
    public void simple() {
        Assert.fail("... TODO ASSERT...");
        showDisplay(new MouseMoveCallback().setCallback(decimalPosition -> {
            TerrainAreaFinder terrainAreaFinder = getWeldBean(TerrainAreaFinder.class);
            terrainAreaFinder.start(decimalPosition, TerrainType.LAND, 130, 250);
            PositionMarker positionMarker = new PositionMarker();
            terrainAreaFinder.getArea().forEach(pathingNodeWrapper -> positionMarker.addRectangle2D(pathingNodeWrapper.getRectangle(), Color.LIGHTGREEN));
            return new Object[]{positionMarker};
        }));
    }


    @Test
    public void simpleNoObstacles() {
        Assert.fail("... TODO ASSERT...");
        TerrainAreaFinder terrainAreaFinder = getWeldBean(TerrainAreaFinder.class);
        terrainAreaFinder.start(new DecimalPosition(220, 500), TerrainType.LAND, 50, 100);
        PositionMarker positionMarker = new PositionMarker();
        terrainAreaFinder.getArea().forEach(pathingNodeWrapper -> {
            positionMarker.addRectangle2D(pathingNodeWrapper.getRectangle(), Color.LIGHTGREEN);
        });
        //showDisplay(positionMarker);
    }

}