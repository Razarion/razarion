package com.btxtech.client.editor.renderer;

import com.btxtech.uiservice.renderer.AbstractSimpleRenderTaskRunner;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.WebGlRenderTask;
import com.google.inject.Inject;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 23.11.2016.
 */
@ApplicationScoped
public class MonitorRenderTaskRunner extends AbstractSimpleRenderTaskRunner<Void> {
    public interface RenderTask extends WebGlRenderTask<Void> {
    }

    @Inject
    private RenderService renderService;
    private boolean showDeep = true;
    private WebGlRenderTask<Void> renderTask;

    public void showMonitor() {
        if(renderTask != null) {
            throw new IllegalStateException("Shadow monitor is already showing");
        }
        renderTask = createRenderTask(RenderTask.class, null);
        renderService.addRenderTaskRunner(this, "Monitor");
    }

    public void hideMonitor() {
        if(renderTask == null) {
            throw new IllegalStateException("Shadow monitor is already showing");
        }
        renderService.removeRenderTaskRunner(this);
        destroyRenderTask(renderTask);
        renderTask = null;
    }

    public boolean isShown() {
        return renderTask != null;
    }


    public boolean isShowDeep() {
        return showDeep;
    }

    public void setShowDeep(boolean showDeep) {
        this.showDeep = showDeep;
    }
}
