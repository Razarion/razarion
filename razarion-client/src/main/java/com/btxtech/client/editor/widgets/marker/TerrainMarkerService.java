package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ViewFieldConfig;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import jsinterop.annotations.JsType;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@JsType
@ApplicationScoped
public class TerrainMarkerService {
    @Inject
    private Instance<TerrainMarkerEditorRenderTaskRunner> renderInstance;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    private TerrainMarkerEditorRenderTaskRunner terrainMarkerEditorRenderTaskRunner;

    public TerrainMarkerService() {
    }

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

}
