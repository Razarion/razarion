package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.uiservice.GroupSelectionFrame;
import com.btxtech.uiservice.cockpit.CockpitMode;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
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
public class SelectionFrameRenderTask extends AbstractRenderTask<GroupSelectionFrame> {
    @Inject
    private CockpitMode cockpitMode;

    @Override
    public boolean isActive() {
        return cockpitMode.hasGroupSelectionFrame();
    }

    public void startGroupSelection(GroupSelectionFrame groupSelectionFrame) {
        clear();
        setupRenderer(groupSelectionFrame);
    }

    public void onMove(GroupSelectionFrame groupSelectionFrame) {
        clear();
        setupRenderer(groupSelectionFrame);
    }

    public void stop() {
        clear();
    }

    private void setupRenderer(GroupSelectionFrame groupSelectionFrame) {
        ModelRenderer<GroupSelectionFrame, CommonRenderComposite<AbstractSelectionFrameRenderUnit, GroupSelectionFrame>, AbstractSelectionFrameRenderUnit, GroupSelectionFrame> modelRenderer = create();
        CommonRenderComposite<AbstractSelectionFrameRenderUnit, GroupSelectionFrame> renderComposite = modelRenderer.create();
        renderComposite.init(groupSelectionFrame);
        renderComposite.setRenderUnit(AbstractSelectionFrameRenderUnit.class);
        modelRenderer.add(RenderUnitControl.SELECTION_FRAME, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
    }
}
