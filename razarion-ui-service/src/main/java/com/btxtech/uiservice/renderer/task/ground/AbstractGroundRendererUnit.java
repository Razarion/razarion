package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.renderer.AbstractRenderUnit;
import com.btxtech.uiservice.terrain.UiTerrainTile;

import java.util.logging.Logger;

/**
 * Created by Beat
 * 07.08.2016.
 */
public abstract class AbstractGroundRendererUnit extends AbstractRenderUnit<UiTerrainTile> {
    private Logger logger = Logger.getLogger(AbstractGroundRendererUnit.class.getName());

    protected abstract void fillBuffersInternal(UiTerrainTile uiTerrainTile);

    protected abstract void draw(UiTerrainTile uiTerrainTile);

    @Override
    public void fillBuffers(UiTerrainTile uiTerrainTile) {
//        if (uiTerrainTile.getTopTextureId() == null) {
//            logger.warning("No TopTextureId in AbstractGroundRendererUnit for: " + helperString());
//            return;
//        }
//        if (uiTerrainTile.getSplattingId() == null) {
//            logger.warning("No SplattingId in AbstractGroundRendererUnit for: " + helperString());
//            return;
//        }
//        if (uiTerrainTile.getBottomTextureId() == null) {
//            logger.warning("No BottomTextureId in AbstractGroundRendererUnit for: " + helperString());
//            return;
//        }
//        if (uiTerrainTile.getBottomBmId() == null) {
//            logger.warning("No BottomBmId in AbstractGroundRendererUnit for: " + helperString());
//            return;
//        }
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
