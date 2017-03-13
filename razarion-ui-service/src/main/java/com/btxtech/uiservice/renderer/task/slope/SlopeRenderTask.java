package com.btxtech.uiservice.renderer.task.slope;

import com.btxtech.shared.gameengine.planet.terrain.slope.Slope;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ColorBufferRenderer;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.DepthBufferRenderer;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.NormRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.btxtech.shared.datatypes.terrain.SlopeUi;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class SlopeRenderTask extends AbstractRenderTask<Slope> {
    @Inject
    @ColorBufferRenderer
    private Instance<AbstractSlopeRendererUnit> rendererInstance;
    @Inject
    @DepthBufferRenderer
    private Instance<AbstractSlopeRendererUnit> depthBufferRendererInstance;
    @Inject
    @NormRenderer
    private Instance<AbstractSlopeRendererUnit> normRendererInstance;
    @Inject
    private TerrainUiService terrainUiService;

    @PostConstruct
    public void postConstruct() {
        setupSlopes(false);
    }

    public void onChanged() {
        clear();
        setupSlopes(true);
    }

    private void setupSlopes(boolean fillBuffer) {
        for (SlopeUi slopeUi : terrainUiService.getSlopes()) {
            ModelRenderer<Slope, CommonRenderComposite<AbstractSlopeRendererUnit, SlopeUi>, AbstractSlopeRendererUnit, SlopeUi> modelRenderer = create();
            CommonRenderComposite<AbstractSlopeRendererUnit, SlopeUi> renderComposite = modelRenderer.create();
            renderComposite.init(slopeUi);
            renderComposite.setRenderUnit(rendererInstance.get());
            renderComposite.setDepthBufferRenderUnit(depthBufferRendererInstance.get());
            renderComposite.setNormRenderUnit(normRendererInstance.get());
            modelRenderer.add(RenderUnitControl.TERRAIN, renderComposite);
            add(modelRenderer);
            if (fillBuffer) {
                renderComposite.fillBuffers();
            }
        }
    }
}
