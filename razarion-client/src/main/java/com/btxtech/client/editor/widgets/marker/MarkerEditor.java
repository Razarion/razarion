package com.btxtech.client.editor.widgets.marker;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.PolygonUtil;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.Camera;
import com.btxtech.uiservice.renderer.ProjectionTransformation;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 29.07.2017.
 */
@ApplicationScoped
public class MarkerEditor implements EditorMouseListener {
    // private Logger logger = Logger.getLogger(MarkerEditor.class.getName());
    private enum Type {
        POLYGON,
        POSITION
    }

    @Inject
    private Camera camera;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private ProjectionTransformation projectionTransformation;
    @Inject
    private TerrainMarkerEditorRenderTask terrainMarkerEditorRenderTask;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    private Type activeType;
    private List<DecimalPosition> polygon;
    private Consumer<List<DecimalPosition>> polygonListener;
    private Consumer<DecimalPosition> decimalPositionListener;
    private Runnable deactivationCallback;

    public void activate(Polygon2D polygon2D, Consumer<List<DecimalPosition>> polygonListener, Runnable deactivationCallback) {
        if (activeType != null) {
            deactivate();
        }
        this.polygonListener = polygonListener;
        this.deactivationCallback = deactivationCallback;
        terrainMouseHandler.setEditorMouseListener(this);
        if (polygon2D != null) {
            polygon = polygon2D.getCorners();
        } else {
            polygon = new ArrayList<>();
        }
        List<DecimalPosition> correctedPolygon = correctPolygon();
        displayPolygon(correctedPolygon);
        activeType = Type.POLYGON;
    }

    public void activate(DecimalPosition decimalPosition, Consumer<DecimalPosition> decimalPositionListener, Runnable deactivationCallback) {
        if (activeType != null) {
            deactivate();
        }
        this.decimalPositionListener = decimalPositionListener;
        this.deactivationCallback = deactivationCallback;
        terrainMouseHandler.setEditorMouseListener(this);
        if (decimalPosition != null) {
            terrainUiService.getTerrainZ(decimalPosition, (ignore, z) -> terrainMarkerEditorRenderTask.showPositionMarker(new Vertex(decimalPosition, z)));
        }
        activeType = Type.POSITION;
    }

    public void deactivate() {
        if (activeType == null) {
            return;
        }
        polygonListener = null;
        decimalPositionListener = null;
        deactivationCallback.run();
        deactivationCallback = null;
        terrainMouseHandler.setEditorMouseListener(null);
        activeType = null;
        terrainMarkerEditorRenderTask.hide();
    }

    @Override
    public void onMouseMove(Vertex terrainPosition) {

    }

    @Override
    public void onMouseDown(Vertex terrainPosition) {
        switch (activeType) {
            case POLYGON:
                polygon.add(terrainPosition.toXY());
                setupPolygon();
                break;
            case POSITION:
                decimalPositionListener.accept(new DecimalPosition(terrainPosition.getX(), terrainPosition.getY()));
                terrainMarkerEditorRenderTask.showPositionMarker(terrainPosition);
                break;
            default:
                throw new IllegalArgumentException("MarkerEditor.onMouseDown() unknown type: " + activeType);
        }
    }

    @Override
    public void onMouseUp() {

    }

    private void setupPolygon() {
        if (polygon.size() < 3) {
            terrainMarkerEditorRenderTask.hide();
            polygonListener.accept(null);
            return;
        }
        List<DecimalPosition> correctedPolygon = correctPolygon();
        displayPolygon(correctedPolygon);
        polygonListener.accept(correctedPolygon);
    }

    private List<DecimalPosition> correctPolygon() {
        List<DecimalPosition> correctedPolygon;
        if (PolygonUtil.isCounterclockwise(polygon)) {
            correctedPolygon = polygon;
        } else {
            correctedPolygon = new ArrayList<>(polygon);
            Collections.reverse(correctedPolygon);
        }

        correctedPolygon = PolygonUtil.removeSelfIntersectingCorners(correctedPolygon);
        return correctedPolygon;
    }

    private void displayPolygon(List<DecimalPosition> polygonToDisplay) {
        if (polygonToDisplay.size() < 3) {
            return;
        }
        List<Vertex> vertexPolygon = polygonToDisplay.stream().map(decimalPosition -> new Vertex(decimalPosition, 0)).collect(Collectors.toList());
        terrainMarkerEditorRenderTask.showPolygon(vertexPolygon);
    }

    public void clear() {
        switch (activeType) {
            case POLYGON:
                polygon.clear();
                setupPolygon();
                break;
            case POSITION:
                break;
            default:
                throw new IllegalArgumentException("MarkerEditor.clear() unknown type: " + activeType);
        }
    }

    public void topView() {
        projectionTransformation.disableFovYConstrain();
        terrainScrollHandler.setPlayGround(null);
        terrainScrollHandler.setScrollDisabled(false, null);
        camera.setTop();
    }


}
