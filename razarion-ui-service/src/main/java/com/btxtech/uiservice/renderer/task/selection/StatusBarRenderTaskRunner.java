package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
import com.btxtech.uiservice.renderer.AbstractRenderTaskRunner;
import com.btxtech.uiservice.renderer.WebGlRenderTask;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 23.01.2017.
 */
@ApplicationScoped
public class StatusBarRenderTaskRunner extends AbstractRenderTaskRunner {
    public interface RenderTask extends WebGlRenderTask<StatusBarGeometry> {
    }

    @Inject
    private ItemMarkerService itemMarkerService;
    @Inject
    private BaseItemUiService baseItemUiService;

    @PostConstruct
    public void postConstruct() {
        setupStatusBar();
    }

    @Override
    protected double setupInterpolationFactor(long timeStamp) {
        return baseItemUiService.setupInterpolationFactor(timeStamp);
    }

    private void setupStatusBar() {
        createModelRenderTask(RenderTask.class,
                new StatusBarGeometry(),
                timeStamp -> itemMarkerService.provideStatusBarModelMatrices(),
                null,
                null,
                null);
    }
}
