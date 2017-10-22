package com.btxtech.shared.gameengine.planet.gui;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
import com.btxtech.shared.gameengine.planet.gui.userobject.PositionMarker;
import com.btxtech.shared.gameengine.planet.terrain.gui.AbstractTerrainTestRenderer;
import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Created by Beat
 * on 19.10.2017.
 */
public class UserDataRenderer {
    private WeldTestRenderer weldTestRenderer;
    private Object[] userObjects;

    public UserDataRenderer(WeldTestRenderer weldTestRenderer, Object[] userObjects) {
        this.weldTestRenderer = weldTestRenderer;
        this.userObjects = userObjects;
    }

    public void render() {
        Arrays.stream(userObjects).forEach(this::renderUserObject);
    }

    private void renderUserObject(Object userObject) {
        if (userObject instanceof SimplePath) {
            render((SimplePath) userObject);
        } else if (userObject instanceof PositionMarker) {
            render((PositionMarker) userObject);
        } else {
            throw new IllegalArgumentException("Unknown userObject: " + userObject);
        }
    }

    private void render(PositionMarker positionMarker) {
        positionMarker.getCircles().forEach(circle -> weldTestRenderer.strokeCircle(circle, AbstractTerrainTestRenderer.FAT_LINE_WIDTH, Color.RED));
        weldTestRenderer.drawPositions(positionMarker.getPositions(), AbstractTerrainTestRenderer.FAT_LINE_WIDTH, Color.RED);
    }

    private void render(SimplePath simplePath) {
        weldTestRenderer.strokeLine(simplePath.getWayPositions(), AbstractTerrainTestRenderer.FAT_LINE_WIDTH, Color.DEEPPINK, true);
    }
}
