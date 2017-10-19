package com.btxtech.shared.gameengine.planet.terrain.gui.weld;

import com.btxtech.shared.gameengine.datatypes.command.SimplePath;
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
        Arrays.stream(userObjects).forEach(this::render);
    }

    private void render(Object userObject) {
        if (userObject instanceof SimplePath) {
            render((SimplePath) userObject);
        } else {
            throw new IllegalArgumentException("Unknown userObject: " + userObject);
        }
    }

    private void render(SimplePath simplePath) {
        weldTestRenderer.strokeLine(simplePath.getWayPositions(), AbstractTerrainTestRenderer.FAT_LINE_WIDTH, Color.DEEPPINK, true);
    }
}
