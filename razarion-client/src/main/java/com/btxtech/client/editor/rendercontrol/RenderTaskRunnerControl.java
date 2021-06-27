package com.btxtech.client.editor.rendercontrol;

import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import jsinterop.annotations.JsType;

/**
 * Created by Beat
 * 26.03.2017.
 */

@JsType
public class RenderTaskRunnerControl {
    public String name;
    public boolean enabled;
    private AbstractRenderTaskRunner abstractRenderTask;

    public RenderTaskRunnerControl(AbstractRenderTaskRunner abstractRenderTask) {
        name = abstractRenderTask.getName();
        enabled = abstractRenderTask.isEnabled();
        this.abstractRenderTask = abstractRenderTask;
    }

    @SuppressWarnings("unused") // Called by Angular
    public String getName() {
        return name;
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isEnabled() {
        return enabled;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        abstractRenderTask.setEnabled(enabled);
    }
}
