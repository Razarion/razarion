package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.SelectionHandler;
import com.btxtech.uiservice.renderer.AbstractModelRenderTaskRunner;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 27.09.2016.
 */
@ApplicationScoped
public class SelectionFrameRenderTask extends AbstractModelRenderTaskRunner<GroupSelectionFrame> {
    @Inject
    private SelectionHandler selectionHandler;
    private boolean active;

    @Override
    public boolean isActive() {
        return active;
    }

    public void startGroupSelection(GroupSelectionFrame groupSelectionFrame) {
        clear();
        setupRenderer(groupSelectionFrame);
        active = true;
    }

    public void onMove(GroupSelectionFrame groupSelectionFrame) {
        clear();
        setupRenderer(groupSelectionFrame);
    }

    public void stop() {
        active = false;
        clear();
    }

    private void setupRenderer(GroupSelectionFrame groupSelectionFrame) {
        ModelRenderer<GroupSelectionFrame> modelRenderer = create();
        CommonRenderComposite<AbstractSelectionFrameRenderUnit, GroupSelectionFrame> renderComposite = modelRenderer.create();
        renderComposite.init(groupSelectionFrame);
        renderComposite.setRenderUnit(AbstractSelectionFrameRenderUnit.class);
        modelRenderer.add(RenderUnitControl.SELECTION_FRAME, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }
}
