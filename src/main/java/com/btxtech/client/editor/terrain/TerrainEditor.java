package com.btxtech.client.editor.terrain;

import com.btxtech.client.TerrainMouseDownEvent;
import com.btxtech.client.TerrainMouseMoveEvent;
import com.btxtech.client.renderer.engine.RenderService;
import com.btxtech.client.terrain.TerrainSurface;
import com.btxtech.game.jsre.client.common.Index;
import com.btxtech.game.jsre.common.MathHelper;
import com.btxtech.shared.TerrainEditorService;
import com.btxtech.shared.dto.SlopeNameId;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.primitives.Polygon2I;
import com.btxtech.shared.primitives.Ray3d;
import com.btxtech.shared.primitives.Vertex;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * 05.05.2016.
 */
@Singleton
public class TerrainEditor {
    private static final int NO_SELECTION = -1;
    private Logger logger = Logger.getLogger(TerrainEditor.class.getName());
    @Inject
    private Event<TerrainEditorCursorPositionEvent> terrainEditorCursorPositionEvent;
    @Inject
    private Event<TerrainEditorCursorShapeEvent> terrainEditorCursorShapeEvent;
    @Inject
    private Event<TerrainEditorSlopeSelectedEvent> terrainEditorSlopeSelectedEvent;
    @Inject
    private Event<TerrainEditorSlopeModifiedEvent> terrainEditorSlopeModifiedEvent;
    @Inject
    private TerrainSurface terrainSurface;
    @Inject
    private Caller<TerrainEditorService> terrainEditorService;
    @Inject
    private RenderService renderService;
    private boolean active;
    private Polygon2I cursor;
    private int cursorRadius = 50;
    private int cursorCorners = 6;
    private int selectedSlopeId = NO_SELECTION;
    private SlopeNameId slope4New;
    private Map<Integer, ModifiedTerrainSlopePosition> modifiedTerrainSlopePositions = new HashMap<>();

    public TerrainEditor() {
        cursor = setupCursor();
    }

    public void onTerrainMouseMoved(@Observes TerrainMouseMoveEvent terrainMouseMoveEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseMoveEvent.getWorldPickRay();
            Vertex terrainPosition = terrainSurface.calculatePositionOnTerrain(ray3d);
            // Cursor
            terrainEditorCursorPositionEvent.fire(new TerrainEditorCursorPositionEvent(terrainPosition));
            // Handle inside polygon
            int selectedSlopeId = NO_SELECTION;
            Polygon2I movedCursor = cursor.translate(terrainPosition.toXY().getPosition());
            for (Map.Entry<Integer, ModifiedTerrainSlopePosition> entry : modifiedTerrainSlopePositions.entrySet()) {
                if (entry.getValue().getPolygon2I().adjoins(movedCursor)) {
                    selectedSlopeId = entry.getKey();
                    break;
                }
            }
            if (selectedSlopeId != this.selectedSlopeId) {
                this.selectedSlopeId = selectedSlopeId;
                terrainEditorSlopeSelectedEvent.fire(new TerrainEditorSlopeSelectedEvent(selectedSlopeId));
            }
        }
    }

    public void onTerrainMouseDown(@Observes TerrainMouseDownEvent terrainMouseDownEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseDownEvent.getWorldPickRay();
            Vertex terrainPosition = terrainSurface.calculatePositionOnTerrain(ray3d);
            Polygon2I movedCursor = cursor.translate(terrainPosition.toXY().getPosition());
            if(hasSelection()) {
                ModifiedTerrainSlopePosition slopePosition = modifiedTerrainSlopePositions.get(selectedSlopeId);
                Polygon2I newPolygon = slopePosition.combine(movedCursor);
                terrainEditorSlopeModifiedEvent.fire(new TerrainEditorSlopeModifiedEvent(selectedSlopeId, newPolygon));
                if(terrainMouseDownEvent.isCtrlDown()) {
                    logger.severe("Polygon: " + newPolygon.testString());
                }
            } else {
                ModifiedTerrainSlopePosition terrainSlopePosition = new ModifiedTerrainSlopePosition(slope4New.getId(), movedCursor);
                int id = modifiedTerrainSlopePositions.size();
                modifiedTerrainSlopePositions.put(id, terrainSlopePosition);
                renderService.createTerrainEditorRenderer(id);
            }
        }
    }

    public void setTerrainSlopePositions(Collection<TerrainSlopePosition> terrainSlopePositions) {
        for (TerrainSlopePosition terrainSlopePosition : terrainSlopePositions) {
            modifiedTerrainSlopePositions.put(modifiedTerrainSlopePositions.size(), new ModifiedTerrainSlopePosition(terrainSlopePosition));
        }
    }

    public void activate() {
        if (active) {
            return;
        }
        active = true;
        renderService.setShowEditor(true);
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        renderService.setShowEditor(false);
    }

    public Collection<Integer> getSlopePolygonIds() {
        return modifiedTerrainSlopePositions.keySet();
    }

    public Polygon2I getSlopePolygon(int id) {
        return modifiedTerrainSlopePositions.get(id).getPolygon2I();
    }

    public int getCursorRadius() {
        return cursorRadius;
    }

    public void setCursorRadius(int cursorRadius) {
        this.cursorRadius = cursorRadius;
        cursorChanged();
    }

    public int getCursorCorners() {
        return cursorCorners;
    }

    public void setCursorCorners(int cursorCorners) {
        this.cursorCorners = cursorCorners;
        cursorChanged();
    }

    private void cursorChanged() {
        cursor = setupCursor();
        terrainEditorCursorShapeEvent.fire(new TerrainEditorCursorShapeEvent(cursor));
    }

    private Polygon2I setupCursor() {
        List<Index> corners = new ArrayList<>();
        double deltaAngle = MathHelper.ONE_RADIANT / cursorCorners;
        for (int i = 0; i < cursorCorners; i++) {
            corners.add(Index.createVector(deltaAngle * i, cursorRadius));
        }
        return new Polygon2I(corners);
    }

    public Polygon2I getCursor() {
        return cursor;
    }

    private boolean hasSelection() {
        return selectedSlopeId != NO_SELECTION;
    }

    public void updateTerrainSurface() {
        Collection<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (Map.Entry<Integer, ModifiedTerrainSlopePosition> entry : modifiedTerrainSlopePositions.entrySet()) {
            terrainSlopePositions.add(entry.getValue().rendererTerrainSlopePosition(entry.getKey()));
        }
        terrainSurface.setTerrainSlopePositions(terrainSlopePositions);
    }

    public void save() {
        Collection<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (ModifiedTerrainSlopePosition modifiedTerrainSlopePosition : modifiedTerrainSlopePositions.values()) {
            terrainSlopePositions.add(modifiedTerrainSlopePosition.serverTerrainSlopePosition());
        }

        terrainEditorService.call(new RemoteCallback<Void>() {
            @Override
            public void callback(Void response) {

            }
        }, new ErrorCallback<Object>() {
            @Override
            public boolean error(Object message, Throwable throwable) {
                logger.log(Level.SEVERE, "saveTerrainSlopePositions failed: " + message, throwable);
                return false;
            }
        }).saveTerrainSlopePositions(terrainSlopePositions);
    }

    public void setSlope4New(SlopeNameId slope4New) {
        this.slope4New = slope4New;
    }
}
