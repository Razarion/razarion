package com.btxtech.client.editor.renderpanel;

import com.btxtech.uiservice.renderer.AbstractRenderTask;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 26.03.2017.
 */
@Bindable
public class RenderTaskModel {
    private String name;
    private AbstractRenderTask abstractRenderTask;
    private boolean enabled;

    /**
     * Used by Errai
     */
    public RenderTaskModel() {
    }

    public RenderTaskModel(AbstractRenderTask abstractRenderTask) {
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
