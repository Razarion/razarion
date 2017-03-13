package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.datatypes.terrain.GroundUi;
import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask<GroundSkeletonConfig> {
    @Inject
    private TerrainUiService terrainUiService;

    @PostConstruct
    public void postConstruct() {
        setup(false);
    }

    public void onChanged() {
        clear();
        setup(true);
    }

    private void setup(boolean fillBuffer) {
        ModelRenderer<GroundUi, CommonRenderComposite<AbstractGroundRendererUnit, GroundUi>, AbstractGroundRendererUnit, GroundUi> modelRenderer = create();
        CommonRenderComposite<AbstractGroundRendererUnit, GroundUi> renderComposite = modelRenderer.create();
        renderComposite.init(terrainUiService.getGroundUi());
        renderComposite.setRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setDepthBufferRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setNormRenderUnit(AbstractGroundRendererUnit.class);
        modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
        add(modelRenderer);
        if (fillBuffer) {
            renderComposite.fillBuffers();
        }
    }
}
