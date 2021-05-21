package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsType
@ApplicationScoped
public class TerrainMarkerService {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private Instance<TerrainMarkerEditorRenderTaskRunner> renderInstance;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    private TerrainMarkerEditorRenderTaskRunner terrainMarkerEditorRenderTaskRunner;

    @SuppressWarnings("unused") // Called by Angular
    public void showPosition(double x, double y) {
        getRunner().showPositionMarker(new Vertex(x, y, 0));
        terrainScrollHandler.executeViewFieldConfig(new ViewFieldConfig().toPosition(new DecimalPosition(x, y)), null);
    }

    @SuppressWarnings("unused") // Called by Angular
    public void activatePositionCursor(PositionCallback positionCallback) {
        terrainMouseHandler.setEditorMouseListener(new EditorMouseListener() {
            @Override
            public void onMouseMove(Vertex terrainPosition, boolean primaryButtonDown) {

            }

            @Override
            public void onMouseDown(Vertex terrainPosition) {
                getRunner().showPositionMarker(new Vertex(terrainPosition.getX(), terrainPosition.getY(), 0));
                positionCallback.position(terrainPosition.getX(), terrainPosition.getY());
            }

            @Override
            public void onMouseUp() {

            }
        });
        // TODO terrainMouseHandler.setEditorMouseListener(null)
    }

    private TerrainMarkerEditorRenderTaskRunner getRunner() {
        if (terrainMarkerEditorRenderTaskRunner == null) {
            terrainMarkerEditorRenderTaskRunner = renderInstance.get();
            renderService.addRenderTaskRunner(terrainMarkerEditorRenderTaskRunner, "Terrain Marker");
        }
        return terrainMarkerEditorRenderTaskRunner;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void showPolygon(Polygon2D polygon2D) {
        try {
            if (polygon2D == null || polygon2D.size() < 3) {
                getRunner().hide();
                return;
            }
            List<Vertex> vertexPolygon = polygon2D.getCorners().stream()
                    .map(decimalPosition -> new Vertex(decimalPosition, 0))
                    .collect(Collectors.toList());
            getRunner().showPolygon(vertexPolygon);
            terrainScrollHandler.executeViewFieldConfig(new ViewFieldConfig().toPosition(polygon2D.toAabb().center()), null);
        } catch (Throwable t) {
            exceptionHandler.handleException(t);
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void activatePolygonCursor(Polygon2D polygon2D, PolygonCallback polygonCallback) {
        List<Vertex> vertexPolygon;
        if (polygon2D == null || polygon2D.size() < 3) {
            vertexPolygon = new ArrayList<>();
        } else {
            vertexPolygon = polygon2D.getCorners().stream()
                    .map(decimalPosition -> new Vertex(decimalPosition, 0))
                    .collect(Collectors.toList());
        }

        terrainMouseHandler.setEditorMouseListener(new EditorMouseListener() {
            @Override
            public void onMouseMove(Vertex terrainPosition, boolean primaryButtonDown) {

            }

            @Override
            public void onMouseDown(Vertex terrainPosition) {
                vertexPolygon.add(terrainPosition);
                if (vertexPolygon.size() < 3) {
                    polygonCallback.polygon(null);
                    return;
                }
                polygonCallback.polygon(new Polygon2D(Vertex.toXY(vertexPolygon)));
                getRunner().showPolygon(vertexPolygon);
            }

            @Override
            public void onMouseUp() {

            }
        });
        // TODO terrainMouseHandler.setEditorMouseListener(null)
    }

}
