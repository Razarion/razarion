package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.shared.datatypes.terrain.WaterUi;
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
public class WaterRenderTask extends AbstractRenderTask<WaterUi> {
    @Inject
    private TerrainUiService terrainUiService;

    @PostConstruct
    public void postConstruct() {
        ModelRenderer<WaterUi, CommonRenderComposite<AbstractWaterRendererUnit, WaterUi>, AbstractWaterRendererUnit, WaterUi> modelRenderer = create();
        CommonRenderComposite<AbstractWaterRendererUnit, WaterUi> renderComposite = modelRenderer.create();
        renderComposite.init(terrainUiService.getWaterUi());
        renderComposite.setRenderUnit(AbstractWaterRendererUnit.class);
        renderComposite.setNormRenderUnit(AbstractWaterRendererUnit.class);
        modelRenderer.add(RenderUnitControl.WATER, renderComposite);
        add(modelRenderer);
    }

}
