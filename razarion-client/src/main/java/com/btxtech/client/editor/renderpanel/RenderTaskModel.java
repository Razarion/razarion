package com.btxtech.client.editor.renderpanel;

import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 26.03.2017.
 */
@Bindable
public class RenderTaskModel {
    private String name;
    private AbstractRenderTaskRunner abstractRenderTask;
    private boolean enabled;

    /**
     * Used by Errai
     */
    public RenderTaskModel() {
    }

    public RenderTaskModel(AbstractRenderTaskRunner abstractRenderTask) {
        name = abstractRenderTask.getName();
        enabled = abstractRenderTask.isEnabled();
        this.abstractRenderTask = abstractRenderTask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        abstractRenderTask.setEnabled(enabled);
    }
}
