package com.btxtech.uiservice.terrain.helpers;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.uiservice.gui.AbstractUiTestGuiRenderer;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import javafx.scene.paint.Color;

/**
 * Created by Beat
 * on 04.07.2017.
 */
public class TestUiTerrainTileRenderer extends AbstractUiTestGuiRenderer {
    private UiTerrainTile uiTerrainTile;

    public TestUiTerrainTileRenderer(UiTerrainTile uiTerrainTile) {
        this.uiTerrainTile = uiTerrainTile;
    }

    @Override
    protected void doRender() {
        for (double x = 0.5; x < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; x++) {
            for (double y = 0.5; y < TerrainUtil.TERRAIN_TILE_ABSOLUTE_LENGTH; y++) {
                if (uiTerrainTile.isTerrainFree(new DecimalPosition(x, y))) {
                    getGc().setFill(Color.GREEN);
                } else {
                    getGc().setFill(Color.RED);
                }
                getGc().fillRect(x - 0.5, y - 0.5, 1, 1);
            }
        }
    }
}
