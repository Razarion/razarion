package com.btxtech.client.editor.widgets.polygon;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.utils.PolygonUtil;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * on 29.07.2017.
 */
@ApplicationScoped
public class PolygonEditor implements EditorMouseListener {
    @Inject
    private TerrainPolygonEditorRenderTask terrainPolygonEditorRenderTask;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    private boolean active;
    private List<DecimalPosition> polygon;
    private Consumer<List<DecimalPosition>> polygonListener;

    public void activate(Polygon2D polygon2D, Consumer<List<DecimalPosition>> polygonListener) {
        this.polygonListener = polygonListener;
        if (active) {
            return;
        }
        terrainMouseHandler.setEditorMouseListener(this);
        if (polygon2D != null) {
            polygon = polygon2D.getCorners();
        } else {
            polygon = new ArrayList<>();
        }
        List<DecimalPosition> correctedPolygon = correctPolygon();
        displayPolygon(correctedPolygon);
        active = true;
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        terrainMouseHandler.setEditorMouseListener(null);
        active = false;
        terrainPolygonEditorRenderTask.hidePolygon();
    }

    @Override
    public void onMouseMove(Vertex terrainPosition) {

    }

    @Override
    public void onMouseDown(Vertex terrainPosition) {
        polygon.add(terrainPosition.toXY());
        setupPolygon();
    }

    @Override
    public void onMouseUp() {

    }

    private void setupPolygon() {
        if (!active) {
            return;
        }
        if (polygon.size() < 3) {
            terrainPolygonEditorRenderTask.hidePolygon();
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
        if(polygonToDisplay.size() < 3) {
            return;
        }
        List<Vertex> vertexPolygon = polygonToDisplay.stream().map(decimalPosition -> new Vertex(decimalPosition, 0)).collect(Collectors.toList());
        terrainPolygonEditorRenderTask.showPolygon(vertexPolygon);
    }

    public void clear() {
        polygon.clear();
        setupPolygon();
    }
}
