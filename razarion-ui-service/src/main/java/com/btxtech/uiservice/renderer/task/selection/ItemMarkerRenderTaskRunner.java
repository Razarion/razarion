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
public class ItemMarkerRenderTaskRunner extends AbstractRenderTaskRunner {
    public interface MarkerRenderTask extends WebGlRenderTask<MarkerGeometry> {
    }

    @Inject
    private ItemMarkerService itemMarkerService;
    @Inject
    private BaseItemUiService baseItemUiService;

    @PostConstruct
    public void postConstruct() {
        setupItemMarker();
    }

    @Override
    protected double setupInterpolationFactor() {
        return baseItemUiService.setupInterpolationFactor();
    }

    private void setupItemMarker() {
        WebGlRenderTask<MarkerGeometry> markerRenderTask = createModelRenderTask(MarkerRenderTask.class,
                new MarkerGeometry(),
                timeStamp -> itemMarkerService.provideMarkerModelMatrices(),
                null,
                null,
                null);
        markerRenderTask.setActive(true);
    }
}
