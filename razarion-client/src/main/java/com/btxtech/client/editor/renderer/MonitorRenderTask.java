package com.btxtech.client.editor.renderer;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.google.inject.Inject;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 23.11.2016.
 */
@ApplicationScoped
public class MonitorRenderTask extends AbstractRenderTask<Void> {
    @Inject
    private RenderService renderService;
    private boolean showDeep = true;

    public void showMonitor() {
        ModelRenderer<Void, CommonRenderComposite<ShadowMonitorRendererUnit, Void>, ShadowMonitorRendererUnit, Void> modelRenderer = create();
        CommonRenderComposite<ShadowMonitorRendererUnit, Void> renderComposite = modelRenderer.create();
        renderComposite.init(null);
        renderComposite.setRenderUnit(ShadowMonitorRendererUnit.class);
        modelRenderer.add(RenderUnitControl.NORMAL, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        renderService.addRenderTask(this, "Monitor");
    }

    public void hideMonitor() {
        renderService.removeRenderTask(this);
        clear();
    }

    public boolean isShown() {
        return renderService.containsRenderTask(this);
    }


    public boolean isShowDeep() {
        return showDeep;
    }

    public void setShowDeep(boolean showDeep) {
        this.showDeep = showDeep;
    }

    @Override
    public boolean castShadow() {
        // Prevent [.WebGL-0000019514AD4DA0] GL_INVALID_OPERATION: Feedback loop formed between Framebuffer and active Texture.
        return false;
    }
}
