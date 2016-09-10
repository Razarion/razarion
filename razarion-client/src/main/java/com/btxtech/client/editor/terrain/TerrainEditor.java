package com.btxtech.client.editor.terrain;

import com.btxtech.client.TerrainKeyDownEvent;
import com.btxtech.client.TerrainKeyUpEvent;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.mouse.TerrainMouseDownEvent;
import com.btxtech.uiservice.mouse.TerrainMouseMoveEvent;
import com.btxtech.client.renderer.engine.ClientRenderServiceImpl;
import com.btxtech.shared.PlanetEditorProvider;
import com.btxtech.shared.datatypes.Index;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Polygon2I;
import com.btxtech.shared.datatypes.Ray3d;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.utils.MathHelper;
import elemental.events.KeyboardEvent;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
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
@ApplicationScoped
public class TerrainEditor {
    public enum CursorType {
        CREATE,
        MODIFY,
        REMOVE_MODE,
        REMOVE
    }

    private static final int NO_SELECTION = -1;
    private Logger logger = Logger.getLogger(TerrainEditor.class.getName());
    @Inject
    private Event<TerrainEditorCursorShapeEvent> terrainEditorCursorShapeEvent;
    @Inject
    private Event<TerrainEditorSlopeModifiedEvent> terrainEditorSlopeModifiedEvent;
    @Inject
    private TerrainService terrainService;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private Caller<PlanetEditorProvider> planetEditorServiceCaller;
    @Inject
    private ClientRenderServiceImpl renderService;
    private boolean active;
    private Polygon2I cursor;
    private int cursorRadius = 200;
    private int cursorCorners = 20;
    private int selectedSlopeId = NO_SELECTION;
    private ObjectNameId slope4New;
    private CursorType cursorType = CursorType.CREATE;
    private Map<Integer, ModifiedTerrainSlopePosition> modifiedTerrainSlopePositions = new HashMap<>();
    private boolean deletePressed;
    private Matrix4 cursorModelMatrix = Matrix4.createIdentity();

    public TerrainEditor() {
        cursor = setupCursor();
    }

    public void onTerrainMouseMoved(@Observes TerrainMouseMoveEvent terrainMouseMoveEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseMoveEvent.getWorldPickRay();
            Vertex terrainPosition = terrainService.calculatePositionOnZeroLevel(ray3d);
            // Cursor
            cursorModelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());

            // Handle inside polygon
            int selectedSlopeId = NO_SELECTION;
            Polygon2I movedCursor = cursor.translate(terrainPosition.toXY().getPosition());
            for (Map.Entry<Integer, ModifiedTerrainSlopePosition> entry : modifiedTerrainSlopePositions.entrySet()) {
                Polygon2I polygon = entry.getValue().getPolygon2I();
                if (polygon != null && polygon.adjoins(movedCursor)) {
                    selectedSlopeId = entry.getKey();
                    break;
                }
            }
            this.selectedSlopeId = selectedSlopeId;
            if (selectedSlopeId != NO_SELECTION) {
                if (deletePressed) {
                    cursorType = CursorType.REMOVE;
                } else {
                    cursorType = CursorType.MODIFY;
                }
            } else {
                if (deletePressed) {
                    cursorType = CursorType.REMOVE_MODE;
                } else {
                    cursorType = CursorType.CREATE;
                }
            }
        }
    }

    public void onTerrainMouseDown(@Observes TerrainMouseDownEvent terrainMouseDownEvent) {
        if (active) {
            Ray3d ray3d = terrainMouseDownEvent.getWorldPickRay();
            Vertex terrainPosition = terrainService.calculatePositionOnZeroLevel(ray3d);
            Polygon2I movedCursor = cursor.translate(terrainPosition.toXY().getPosition());
            if (hasSelection()) {
                ModifiedTerrainSlopePosition slopePosition = modifiedTerrainSlopePositions.get(selectedSlopeId);
                if (deletePressed) {
                    Polygon2I newPolygon = slopePosition.remove(movedCursor);
                    if (newPolygon != null) {
                        terrainEditorSlopeModifiedEvent.fire(new TerrainEditorSlopeModifiedEvent(selectedSlopeId, newPolygon));
                    } else {
                        renderService.removeTerrainEditorRenderer(selectedSlopeId);
                    }
                } else {
                    Polygon2I newPolygon = slopePosition.combine(movedCursor);
                    terrainEditorSlopeModifiedEvent.fire(new TerrainEditorSlopeModifiedEvent(selectedSlopeId, newPolygon));
                }
            } else {
                if (!deletePressed) {
                    ModifiedTerrainSlopePosition slopePosition = new ModifiedTerrainSlopePosition(slope4New.getId(), movedCursor);
                    int id = modifiedTerrainSlopePositions.size();
                    modifiedTerrainSlopePositions.put(id, slopePosition);
                    renderService.createTerrainEditorRenderer(id);
                }
            }
        }
    }

    public void onTerrainKeyDown(@Observes TerrainKeyDownEvent terrainKeyDownEvent) {
        if (terrainKeyDownEvent.getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
            deletePressed = true;
            if (selectedSlopeId != NO_SELECTION) {
                cursorType = CursorType.REMOVE;
            } else {
                cursorType = CursorType.REMOVE_MODE;
            }
        }
    }

    public void onTerrainKeyUp(@Observes TerrainKeyUpEvent terrainKeyUpEvent) {
        if (terrainKeyUpEvent.getKeyboardEvent().getKeyCode() == KeyboardEvent.KeyCode.DELETE) {
            deletePressed = false;
            if (selectedSlopeId != NO_SELECTION) {
                cursorType = CursorType.MODIFY;
            } else {
                cursorType = CursorType.CREATE;
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
        renderService.setShowSlopeEditor(true);
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        renderService.setShowSlopeEditor(false);
    }

    public Collection<Integer> getSlopePolygonIds() {
        Collection<Integer> ids = new ArrayList<>();
        for (Map.Entry<Integer, ModifiedTerrainSlopePosition> entry : modifiedTerrainSlopePositions.entrySet()) {
            if (entry.getValue().getPolygon2I() != null) {
                ids.add(entry.getKey());
            }
        }
        return ids;
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
            ModifiedTerrainSlopePosition modifiedTerrainSlopePosition = entry.getValue();
            if (modifiedTerrainSlopePosition.getPolygon2I() != null) {
                terrainSlopePositions.add(modifiedTerrainSlopePosition.createRendererTerrainSlopePosition(entry.getKey()));
            }
        }
        // TODO terrainService.setTerrainSlopePositions(terrainSlopePositions);
    }

    public void save() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (ModifiedTerrainSlopePosition modifiedTerrainSlopePosition : modifiedTerrainSlopePositions.values()) {
            if (modifiedTerrainSlopePosition.isValidForServer()) {
                terrainSlopePositions.add(modifiedTerrainSlopePosition.createServerTerrainSlopePosition());
            }
        }

        planetEditorServiceCaller.call(new RemoteCallback<Void>() {
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

    public void setSlope4New(ObjectNameId slope4New) {
        this.slope4New = slope4New;
    }

    public Matrix4 getCursorModelMatrix() {
        return cursorModelMatrix;
    }

    public int getSelectedSlopeId() {
        return selectedSlopeId;
    }

    public CursorType getCursorType() {
        return cursorType;
    }
}
