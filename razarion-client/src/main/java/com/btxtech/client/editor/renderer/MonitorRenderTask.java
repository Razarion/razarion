package com.btxtech.client.editor.renderer;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderSubTask;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.google.inject.Inject;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 23.11.2016.
 */
@ApplicationScoped
public class MonitorRenderTask extends AbstractRenderTask<Void> {
    public interface SubTask extends RenderSubTask<Void> {
    }

    @Inject
    private RenderService renderService;
    private boolean showDeep = true;
    private ModelRenderer<Void> modelRenderer;

    public void showMonitor() {
        if(modelRenderer != null) {
            throw new IllegalStateException("Shadow monitor is already showing");
        }
        modelRenderer = createNew();
        modelRenderer.create(RenderUnitControl.NORMAL, MonitorRenderTask.SubTask.class, null);
        renderService.addRenderTask(this, "Monitor");
    }

    public void hideMonitor() {
        if(modelRenderer == null) {
            throw new IllegalStateException("Shadow monitor is already showing");
        }
        renderService.removeRenderTask(this);
        destroy(modelRenderer);
        modelRenderer = null;
    }

    public boolean isShown() {
        return modelRenderer != null;
    }


    public boolean isShowDeep() {
        return showDeep;
    }

    public void setShowDeep(boolean showDeep) {
        this.showDeep = showDeep;
    }
}
