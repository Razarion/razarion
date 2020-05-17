package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.UiTerrainGroundTile;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<UiTerrainGroundTile> {
//    private Logger logger = Logger.getLogger(AbstractGroundRendererUnit.class.getName());

    protected abstract void fillBuffersInternal(UiTerrainGroundTile uiTerrainGroundTile);

    protected abstract void draw(UiTerrainGroundTile uiTerrainGroundTile);

    @Override
    public void fillBuffers(UiTerrainGroundTile uiTerrainGroundTile) {
        fillBuffersInternal(uiTerrainGroundTile);
    }

    @Override
    protected void prepareDraw() {
        // Ignore
    }

    @Override
    protected void draw(ModelMatrices modelMatrices) {
        draw(getRenderData());
    }

    @Override
    public String helperString() {
        return "Ground";
    }
}
