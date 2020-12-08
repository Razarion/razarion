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
    public interface StatusBarRenderTask extends WebGlRenderTask<StatusBarGeometry> {
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
    protected double setupInterpolationFactor() {
        return baseItemUiService.setupInterpolationFactor();
    }

    private void setupStatusBar() {
        WebGlRenderTask<StatusBarGeometry> statusBarRenderTask = createModelRenderTask(StatusBarRenderTask.class,
                new StatusBarGeometry(),
                timeStamp -> itemMarkerService.provideStatusBarModelMatrices(),
                null,
                null,
                null);
        statusBarRenderTask.setActive(true);
    }
}
