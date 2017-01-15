package com.btxtech.client.editor.terrain;

import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.terrain.renderer.TerrainEditorRenderTask;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.TerrainEditor;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2016.
 */
@ApplicationScoped
public class TerrainEditorImpl implements TerrainEditor {
    public enum CursorType {
        CREATE,
        MODIFY,
        REMOVE_MODE,
        REMOVE
    }

    private Logger logger = Logger.getLogger(TerrainEditorImpl.class.getName());
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private Caller<PlanetEditorProvider> planetEditorServiceCaller;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private KeyboardEventHandler keyboardEventHandler;
    @Inject
    private TerrainEditorRenderTask terrainEditorRenderTask;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    private boolean active;
    private Polygon2D cursor;
    private double cursorRadius = 10;
    private int cursorCorners = 20;
    private ObjectNameId slope4New;
    private ModifiedSlope selection;
    private CursorType cursorType = CursorType.CREATE;
    private Collection<ModifiedSlope> modifiedSlopes;
    private boolean deletePressed;
    private Matrix4 cursorModelMatrix = Matrix4.createIdentity();

    public TerrainEditorImpl() {
    }

    @Override
    public void onMouseMove(Vertex terrainPosition) {
        // Cursor
        cursorModelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());

        // Handle inside polygon
        ModifiedSlope selection = null;
        Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            if (selection == null) {
                Polygon2D polygon = modifiedSlope.getPolygon();
                if (polygon != null && polygon.adjoins(movedCursor)) {
                    modifiedSlope.setSelected(true);
                    selection = modifiedSlope;
                } else {
                    modifiedSlope.setSelected(false);
                }
            } else {
                modifiedSlope.setSelected(false);
            }
        }
        this.selection = selection;
        if (selection != null) {
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

    @Override
    public void onMouseDown(Vertex terrainPosition) {
        Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
        if (selection != null) {
            if (deletePressed) {
                Polygon2D newPolygon = selection.remove(movedCursor);
                if (newPolygon != null) {
                    terrainEditorRenderTask.updateSlope(selection);
                } else {
                    terrainEditorRenderTask.removeSlope(selection);
                }
            } else {
                selection.combine(movedCursor);
                terrainEditorRenderTask.updateSlope(selection);
            }
        } else {
            if (!deletePressed) {
                ModifiedSlope slopePosition = new ModifiedSlope(slope4New.getId(), movedCursor);
                modifiedSlopes.add(slopePosition);
                terrainEditorRenderTask.newSlope(slopePosition);
            }
        }
    }

    @Override
    public void onDeleteKeyDown(boolean down) {
        deletePressed = down;
        if (down) {
            if (selection != null) {
                cursorType = CursorType.REMOVE;
            } else {
                cursorType = CursorType.REMOVE_MODE;
            }
        } else {
            if (selection != null) {
                cursorType = CursorType.MODIFY;
            } else {
                cursorType = CursorType.CREATE;
            }
        }
    }

    public void activate() {
        if (active) {
            return;
        }
        active = true;
        cursor = setupCursor();
        modifiedSlopes = setupModifiedSlopes();
        renderService.addRenderTask(terrainEditorRenderTask);
        terrainMouseHandler.setTerrainEditor(this);
        keyboardEventHandler.setTerrainEditor(this);
        terrainEditorRenderTask.activate(cursor, modifiedSlopes);
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        cursor = null;
        modifiedSlopes = null;
        renderService.removeRenderTask(terrainEditorRenderTask);
        terrainMouseHandler.setTerrainEditor(null);
        keyboardEventHandler.setTerrainEditor(null);
        terrainEditorRenderTask.deactivate();
    }

    double getCursorRadius() {
        return cursorRadius;
    }

    public void setCursorRadius(double cursorRadius) {
        this.cursorRadius = cursorRadius;
        onCursorChanged();
    }

    int getCursorCorners() {
        return cursorCorners;
    }

    public void setCursorCorners(int cursorCorners) {
        this.cursorCorners = cursorCorners;
        onCursorChanged();
    }

    private void onCursorChanged() {
        cursor = setupCursor();
        terrainEditorRenderTask.changeCursor(cursor);
    }

    private Polygon2D setupCursor() {
        List<DecimalPosition> corners = new ArrayList<>();
        double deltaAngle = MathHelper.ONE_RADIANT / cursorCorners;
        for (int i = 0; i < cursorCorners; i++) {
            corners.add(DecimalPosition.createVector(deltaAngle * (double) i, cursorRadius));
        }
        return new Polygon2D(corners);
    }


    private Collection<ModifiedSlope> setupModifiedSlopes() {
        return gameUiControl.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().getTerrainSlopePositions().stream().map(ModifiedSlope::new).collect(Collectors.toList());
    }

    public void sculpt() {
        List<TerrainSlopePosition> terrainSlopePositions = new ArrayList<>();
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            if (!modifiedSlope.isEmpty()) {
                terrainSlopePositions.add(modifiedSlope.createServerTerrainSlopePositionNoId());
            }
        }
        gameUiControl.getGameUiControlConfig().getGameEngineConfig().getPlanetConfig().setTerrainSlopePositions(terrainSlopePositions);
        terrainUiService.init(gameUiControl.getGameUiControlConfig());
        renderService.setup();
    }

    public void save() {
        List<TerrainSlopePosition> createdSlopes = new ArrayList<>();
        List<TerrainSlopePosition> updatedSlopes = new ArrayList<>();
        List<Integer> deletedSlopeIds = new ArrayList<>();
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            if (modifiedSlope.isCreated()) {
                if (!modifiedSlope.isEmpty()) {
                    createdSlopes.add(modifiedSlope.createServerTerrainSlopePositionNoId());
                }
            } else {
                if (modifiedSlope.isEmpty()) {
                    deletedSlopeIds.add(modifiedSlope.getOriginalId());
                } else if (modifiedSlope.isDirty()) {
                    updatedSlopes.add(modifiedSlope.createServerTerrainSlopePosition());
                }
            }
        }

        if (!createdSlopes.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Attention", "Reload Browser now!"), (message, throwable) -> {
                logger.log(Level.SEVERE, "createTerrainSlopePositions failed: " + message, throwable);
                return false;
            }).createTerrainSlopePositions(createdSlopes);
        }
        if (!updatedSlopes.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Attention", "Reload Browser now!"), (message, throwable) -> {
                logger.log(Level.SEVERE, "updateTerrainSlopePositions failed: " + message, throwable);
                return false;
            }).updateTerrainSlopePositions(updatedSlopes);
        }
        if (!deletedSlopeIds.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Attention", "Reload Browser now!"), (message, throwable) -> {
                logger.log(Level.SEVERE, "deleteTerrainSlopePositions failed: " + message, throwable);
                return false;
            }).deleteTerrainSlopePositions(deletedSlopeIds);
        }
    }

    public void setSlope4New(ObjectNameId slope4New) {
        this.slope4New = slope4New;
    }

    public Matrix4 getCursorModelMatrix() {
        return cursorModelMatrix;
    }

    public CursorType getCursorType() {
        return cursorType;
    }
}
