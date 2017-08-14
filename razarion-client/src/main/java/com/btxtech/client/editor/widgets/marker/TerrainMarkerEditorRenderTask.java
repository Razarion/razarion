package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.renderer.AbstractRenderTask;
import com.btxtech.uiservice.renderer.CommonRenderComposite;
import com.btxtech.uiservice.renderer.ModelRenderer;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.renderer.RenderUnitControl;
import com.google.inject.Inject;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 09.12.2016.
 */
@ApplicationScoped
public class TerrainMarkerEditorRenderTask extends AbstractRenderTask<List<Vertex>> {
    private Logger logger = Logger.getLogger(TerrainMarkerEditorRenderTask.class.getName());
    private static final double POSITION_MARKER_HALF_EDGE = 0.2;
    private static final double POSITION_MARKER_HALF_HEIGHT = 20;
    @Inject
    private RenderService renderService;

    public void showPolygon(List<Vertex> polygon) {
        List<Vertex> triangles = new ArrayList<>();

        Triangulator.calculate(polygon, (vertex1, vertex2, vertex3) -> {
            triangles.add(vertex1);
            triangles.add(vertex2);
            triangles.add(vertex3);
        });

        showTriangles(triangles);
    }

    public void showPositionMarker(Vertex terrainPosition) {
        Vertex bbl = new Vertex(terrainPosition.getX() - POSITION_MARKER_HALF_EDGE, terrainPosition.getY() - POSITION_MARKER_HALF_EDGE, terrainPosition.getZ());
        Vertex bbr = new Vertex(terrainPosition.getX() + POSITION_MARKER_HALF_EDGE, terrainPosition.getY() - POSITION_MARKER_HALF_EDGE, terrainPosition.getZ());
        Vertex btr = new Vertex(terrainPosition.getX() + POSITION_MARKER_HALF_EDGE, terrainPosition.getY() + POSITION_MARKER_HALF_EDGE, terrainPosition.getZ());
        Vertex btl = new Vertex(terrainPosition.getX() - POSITION_MARKER_HALF_EDGE, terrainPosition.getY() + POSITION_MARKER_HALF_EDGE, terrainPosition.getZ());

        Vertex tbl = new Vertex(terrainPosition.getX() - POSITION_MARKER_HALF_EDGE, terrainPosition.getY() - POSITION_MARKER_HALF_EDGE, terrainPosition.getZ() + POSITION_MARKER_HALF_HEIGHT);
        Vertex tbr = new Vertex(terrainPosition.getX() + POSITION_MARKER_HALF_EDGE, terrainPosition.getY() - POSITION_MARKER_HALF_EDGE, terrainPosition.getZ() + POSITION_MARKER_HALF_HEIGHT);
        Vertex ttr = new Vertex(terrainPosition.getX() + POSITION_MARKER_HALF_EDGE, terrainPosition.getY() + POSITION_MARKER_HALF_EDGE, terrainPosition.getZ() + POSITION_MARKER_HALF_HEIGHT);
        Vertex ttl = new Vertex(terrainPosition.getX() - POSITION_MARKER_HALF_EDGE, terrainPosition.getY() + POSITION_MARKER_HALF_EDGE, terrainPosition.getZ() + POSITION_MARKER_HALF_HEIGHT);

        List<Vertex> marker = new ArrayList<>();
        marker.addAll(GeometricUtil.generatePlane(bbl, bbr, tbr, tbl));
        marker.addAll(GeometricUtil.generatePlane(bbr, btr, ttr, tbr));
        marker.addAll(GeometricUtil.generatePlane(btr, btl, ttl, ttr));
        marker.addAll(GeometricUtil.generatePlane(btl, bbl, tbl, ttl));
        showTriangles(marker);
    }

    private void showTriangles(List<Vertex> triangles) {
        hide();
        ModelRenderer<List<Vertex>, CommonRenderComposite<TerrainMarkerEditorRendererUnit, List<Vertex>>, TerrainMarkerEditorRendererUnit, List<Vertex>> modelRenderer = create();
        CommonRenderComposite<TerrainMarkerEditorRendererUnit, List<Vertex>> renderComposite = modelRenderer.create();
        renderComposite.init(triangles);
        renderComposite.setRenderUnit(TerrainMarkerEditorRendererUnit.class);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        renderService.addRenderTask(this, "Editor Terrain Marker");
    }

    public void hide() {
        renderService.removeRenderTask(this);
        clear();
    }

}
