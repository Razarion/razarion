package com.btxtech.scenariongui.scenario;

import com.btxtech.ExtendedGraphicsContext;
import com.btxtech.shared.datatypes.Circle2D;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.utils.GeometricUtil;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Created by Beat
 * 26.11.2016.
 */
public class FillCircleScenario extends Scenario {
    private static final int TILE_SIZE = 8;
    private DecimalPosition position = new DecimalPosition(0, 0);

    @Override
    public void render(ExtendedGraphicsContext extendedGraphicsContext) {
        Circle2D circle = new Circle2D(position, 20);

        List<Index> tiles = GeometricUtil.rasterizeCircle(circle, TILE_SIZE);
        extendedGraphicsContext.getGc().setFill(Color.GREEN);
        for (Index tile : tiles) {
            extendedGraphicsContext.getGc().fillRect(tile.getX() * TILE_SIZE, tile.getY() * TILE_SIZE, TILE_SIZE - 0.05, TILE_SIZE - 0.05);
        }

        extendedGraphicsContext.getGc().setLineWidth(0.1);
        extendedGraphicsContext.getGc().setStroke(Color.RED);
        extendedGraphicsContext.getGc().strokeOval(circle.getCenter().getX() - circle.getRadius(), circle.getCenter().getY() - circle.getRadius(), 2 * circle.getRadius(), 2 * circle.getRadius());
    }

    @Override
    public boolean onMouseMove(DecimalPosition position) {
        this.position = position;
        return true;
    }
}
