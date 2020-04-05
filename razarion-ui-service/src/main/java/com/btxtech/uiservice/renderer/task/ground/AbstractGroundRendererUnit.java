package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<UiTerrainTile> {
//    private Logger logger = Logger.getLogger(AbstractGroundRendererUnit.class.getName());

    protected abstract void fillBuffersInternal(UiTerrainTile uiTerrainTile);

    protected abstract void draw(UiTerrainTile uiTerrainTile);

    @Override
    public void fillBuffers(UiTerrainTile uiTerrainTile) {
        fillBuffersInternal(uiTerrainTile);
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
