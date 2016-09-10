package com.btxtech.uiservice.renderer.task.ground;

import com.btxtech.shared.dto.GroundConfig;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderOrder;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

/**
 * Created by Beat
 * 31.08.2016.
 */
@Singleton
public class GroundRenderTask extends AbstractRenderTask<GroundConfig> {

    @PostConstruct
    public void postConstruct() {
        ModelRenderer<GroundConfig, CommonRenderComposite<AbstractGroundRendererUnit, GroundConfig>, AbstractGroundRendererUnit, GroundConfig> modelRenderer = create();
        CommonRenderComposite<AbstractGroundRendererUnit, GroundConfig> renderComposite = modelRenderer.create();
        renderComposite.init(null);
        renderComposite.setRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setDepthBufferRenderUnit(AbstractGroundRendererUnit.class);
        renderComposite.setNormRenderUnit(AbstractGroundRendererUnit.class);
        modelRenderer.add(RenderOrder.NORMAL, renderComposite);
        add(modelRenderer);
    }
}
