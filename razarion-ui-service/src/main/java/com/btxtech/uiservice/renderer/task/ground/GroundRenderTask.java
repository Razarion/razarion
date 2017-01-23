package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.dto.GroundSkeletonConfig;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

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
    private TerrainTypeService terrainTypeService;

    @PostConstruct
    public void postConstruct() {
        setup(false);
    }

    public void onChanged() {
        clear();
        setup(true);
    }

    private void setup(boolean fillBuffer) {
        ModelRenderer<GroundSkeletonConfig, CommonRenderComposite<AbstractGroundRendererUnit, GroundSkeletonConfig>, AbstractGroundRendererUnit, GroundSkeletonConfig> modelRenderer = create();
        CommonRenderComposite<AbstractGroundRendererUnit, GroundSkeletonConfig> renderComposite = modelRenderer.create();
        renderComposite.init(terrainTypeService.getGroundSkeletonConfig());
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
