package com.btxtech.client.editor.widgets.polygon;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.google.inject.Inject;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

/**
 * Created by Beat
 * 09.12.2016.
 */
@ApplicationScoped
public class TerrainPolygonEditorRenderTask extends AbstractRenderTask<List<Vertex>> {
    @Inject
    private RenderService renderService;

    public void showPolygon(List<Vertex> polygon) {
        hidePolygon();
        ModelRenderer<List<Vertex>, CommonRenderComposite<TerrainPolygonEditorRendererUnit, List<Vertex>>, TerrainPolygonEditorRendererUnit, List<Vertex>> modelRenderer = create();
        CommonRenderComposite<TerrainPolygonEditorRendererUnit, List<Vertex>> renderComposite = modelRenderer.create();
        renderComposite.init(polygon);
        renderComposite.setRenderUnit(TerrainPolygonEditorRendererUnit.class);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        renderService.addRenderTask(this, "Editor Terrain Marker");
    }

    public void hidePolygon() {
        renderService.removeRenderTask(this);
        clear();
    }

}
