package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 27.09.2016.
 */
@ApplicationScoped
public class SelectionFrameRenderTaskRunner extends AbstractRenderTaskRunner {
    public interface RenderTask extends WebGlRenderTask<GroupSelectionFrame> {
    }

    private RenderTask renderTask;

    public void startGroupSelection(GroupSelectionFrame groupSelectionFrame) {
        if (groupSelectionFrame.getCorners() == null) {
            return;
        }
        if (renderTask != null) {
            throw new IllegalStateException("SelectionFrameRenderTaskRunner is already showing");
        }
        renderTask = createRenderTask(RenderTask.class, groupSelectionFrame);
    }

    public void onMove(GroupSelectionFrame groupSelectionFrame) {
        // TODO performance: don't create a new WebGl Program on any mouse pointer move. Only put new geometry array to the shader
        stop();
        startGroupSelection(groupSelectionFrame);
    }

    public void stop() {
        if (renderTask == null) {
            return;
        }
        destroyRenderTask(renderTask);
        renderTask = null;
    }
}
