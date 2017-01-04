package com.btxtech.uiservice.renderer.task.water;

import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.shared.gameengine.planet.terrain.Water;
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
public class WaterRenderTask extends AbstractRenderTask<Water> {
    @Inject
    private TerrainUiService terrainUiService;

    @PostConstruct
    public void postConstruct() {
        ModelRenderer<Water, CommonRenderComposite<AbstractWaterRendererUnit, Water>, AbstractWaterRendererUnit, Water> modelRenderer = create();
        CommonRenderComposite<AbstractWaterRendererUnit, Water> renderComposite = modelRenderer.create();
        renderComposite.init(terrainUiService.getWater());
        renderComposite.setRenderUnit(AbstractWaterRendererUnit.class);
        renderComposite.setNormRenderUnit(AbstractWaterRendererUnit.class);
        modelRenderer.add(RenderUnitControl.WATER, renderComposite);
        add(modelRenderer);
    }

}
