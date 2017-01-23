package com.btxtech.uiservice.renderer.task.selection;

import com.btxtech.uiservice.item.BaseItemUiService;
import com.btxtech.uiservice.item.ItemMarkerService;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderUnitControl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 23.01.2017.
 */
@ApplicationScoped
public class ItemMarkerRenderTask extends AbstractRenderTask<Void> {
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
        ModelRenderer<Void, CommonRenderComposite<AbstractSelectedMarkerRendererUnit, Void>, AbstractSelectedMarkerRendererUnit, Void> modelRenderer = create();
        modelRenderer.init(null, timeStamp -> itemMarkerService.provideSelectedModelMatrices());
        CommonRenderComposite<AbstractSelectedMarkerRendererUnit, Void> compositeRenderer = modelRenderer.create();
        compositeRenderer.init(null);
        compositeRenderer.setRenderUnit(AbstractSelectedMarkerRendererUnit.class);
        modelRenderer.add(RenderUnitControl.SELECTED_ITEM, compositeRenderer);
        add(modelRenderer);
        compositeRenderer.fillBuffers();
    }

    @Override
    protected boolean isActive() {
        return itemMarkerService.haSelection();
    }
}
