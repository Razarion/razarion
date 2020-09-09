package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.datatypes.Triangulator;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.planet.terrain.TerrainTileFactory;
import com.btxtech.shared.utils.GeometricUtil;
import com.btxtech.uiservice.renderer.AbstractModelRenderTask;
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
public class TerrainMarkerEditorRenderTask extends AbstractModelRenderTask<List<Vertex>> {
    private Logger logger = Logger.getLogger(TerrainMarkerEditorRenderTask.class.getName());
    private static final double POSITION_MARKER_HALF_EDGE = 0.2;
    private static final double POSITION_MARKER_HALF_HEIGHT = 20;
    @Inject
    private RenderService renderService;

    public void showPolygon(List<Vertex> polygon) {
        List<Vertex> triangles = new ArrayList<>();

        Triangulator.calculate(polygon, TerrainTileFactory.IGNORE_SMALLER_TRIANGLE, (vertex1, vertex2, vertex3) -> {
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

    public void showRectangleMarker(Rectangle2D rectangle2D) {
        Vertex bl = new Vertex(rectangle2D.getStart().getX(), rectangle2D.getStart().getY(), 0);
        Vertex br = new Vertex(rectangle2D.getStart().getX() + rectangle2D.width(), rectangle2D.getStart().getY(), 0);
        Vertex tr = new Vertex(rectangle2D.getStart().getX() + rectangle2D.width(), rectangle2D.getStart().getY() + rectangle2D.height(), 0);
        Vertex tl = new Vertex(rectangle2D.getStart().getX(), rectangle2D.getStart().getY() + rectangle2D.height(), 0);

        List<Vertex> marker = new ArrayList<>();
        marker.addAll(GeometricUtil.generatePlane(bl, br, tr, tl));
        showTriangles(marker);
    }

    private void showTriangles(List<Vertex> triangles) {
        hide();
        ModelRenderer<List<Vertex>> modelRenderer = create();
        CommonRenderComposite<TerrainMarkerEditorRendererUnit, List<Vertex>> renderComposite = modelRenderer.create();
        renderComposite.init(triangles);
        renderComposite.setRenderUnit(TerrainMarkerEditorRendererUnit.class);
        modelRenderer.add(RenderUnitControl.START_POINT_CIRCLE, renderComposite);
        add(modelRenderer);
        renderComposite.fillBuffers();
        renderService.addRenderTaskRunner(this, "Editor Terrain Marker");
    }

    public void hide() {
        renderService.removeRenderTaskRunner(this);
        clear();
    }

}
