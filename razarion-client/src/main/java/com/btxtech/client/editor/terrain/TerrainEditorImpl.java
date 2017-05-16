package com.btxtech.client.editor.terrain;

import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.terrain.renderer.TerrainEditorRenderTask;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.TerrainEditor;
import com.btxtech.uiservice.control.GameEngineControl;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.nativejs.NativeMatrixFactory;
import com.btxtech.uiservice.renderer.RenderService;
import com.btxtech.uiservice.terrain.TerrainUiService;
import org.jboss.errai.common.client.api.Caller;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
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
        REMOVE_MODE
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
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private GameEngineControl gameEngineControl;
    private boolean active;
    private boolean newSlopeMode = true;
    private Polygon2D cursor;
    private double cursorRadius = 10;
    private int cursorCorners = 20;
    private ObjectNameId slope4New;
    private ObjectNameId terrainObject4New;
    private ModifiedSlope hoverSlope;
    private ModifiedTerrainObject hoverTerrainObject;
    private ModifiedTerrainObject modifyingTerrainObject;
    private CursorType cursorType = CursorType.CREATE;
    private Collection<ModifiedSlope> modifiedSlopes;
    private Collection<ModifiedTerrainObject> modifiedTerrainObjects;
    private boolean deletePressed;
    private Matrix4 cursorModelMatrix = Matrix4.createIdentity();
    private Vertex terrainPosition;
    private List<ModelMatrices> terrainObjectModelMatrices;
    private double terrainObjectRandomZRotation = Math.toDegrees(180);
    private double terrainObjectRandomScale = 1.5;
    private Consumer<Vertex> terrainPositionListener;

    @Override
    public void onMouseMove(Vertex terrainPosition) {
        // Cursor
        cursorModelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());
        this.terrainPosition = terrainPosition;

        if (modifyingTerrainObject != null) {
            modifyingTerrainObject.setNewPosition(terrainPosition, nativeMatrixFactory);
        } else {
            dehoverAll();
            hoverTerrainObject = getTerrainObjectAtTerrain(terrainPosition);
            if (hoverTerrainObject != null) {
                hoverTerrainObject.setHover(true);
                hoverSlope = null;
            } else {
                hoverSlope = getSlopeAtTerrain(terrainPosition);
                if (hoverSlope != null) {
                    hoverSlope.setHover(true);
                }
            }

            if (hoverSlope != null) {
                if (deletePressed) {
                    cursorType = CursorType.REMOVE_MODE;
                } else {
                    cursorType = CursorType.MODIFY;
                }
            } else if (hoverTerrainObject == null) {
                if (newSlopeMode) {
                    if (!deletePressed) {
                        cursorType = CursorType.CREATE;
                    }
                }
            }
        }
    }

    @Override
    public void onMouseDown(Vertex terrainPosition) {
        if (hoverTerrainObject != null) {
            if (deletePressed) {
                hoverTerrainObject.setDeleted();
                hoverTerrainObject = null;
                modifyingTerrainObject = null;
                terrainObjectModelMatrices = setupModelMatrices();
            } else {
                modifyingTerrainObject = hoverTerrainObject;
            }
        } else if (hoverSlope != null) {
            Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
            if (deletePressed) {
                Polygon2D newPolygon = hoverSlope.remove(movedCursor);
                if (newPolygon != null) {
                    terrainEditorRenderTask.updateSlope(hoverSlope);
                } else {
                    terrainEditorRenderTask.removeSlope(hoverSlope);
                }
            } else {
                hoverSlope.combine(movedCursor);
                terrainEditorRenderTask.updateSlope(hoverSlope);
            }
        } else {
            if (!deletePressed) {
                if (newSlopeMode) {
                    ModifiedSlope slopePosition = new ModifiedSlope(slope4New.getId(), cursor.translate(terrainPosition.toXY()));
                    modifiedSlopes.add(slopePosition);
                    terrainEditorRenderTask.newSlope(slopePosition);
                } else {
                    if (terrainObjectRandomScale < 1.0) {
                        throw new IllegalArgumentException("terrainObjectRandomScale < 1.0: " + terrainObjectRandomScale);
                    }
                    double scale = 1.0 / terrainObjectRandomScale + (terrainObjectRandomScale - 1.0 / terrainObjectRandomScale) * Math.random();
                    double rotationZ = Math.toRadians(terrainObjectRandomZRotation) * (2.0 * Math.random() - 1.0);
                    double radius = terrainTypeService.getTerrainObjectConfig(terrainObject4New.getId()).getRadius();
                    ModifiedTerrainObject objectPosition = new ModifiedTerrainObject(terrainObject4New.getId(), terrainPosition.toXY(), scale, rotationZ, radius);
                    modifyingTerrainObject = objectPosition;
                    hoverTerrainObject = objectPosition;
                    modifiedTerrainObjects.add(objectPosition);
                    terrainObjectModelMatrices = setupModelMatrices();
                }
            }
        }
    }

    @Override
    public void onMouseUp() {
        modifyingTerrainObject = null;
    }

    @Override
    public void onDeleteKeyDown(boolean down) {
        deletePressed = down;
        if (down) {
            if (hoverSlope != null) {
                cursorType = CursorType.REMOVE_MODE;
            }
        } else {
            if (hoverSlope != null) {
                cursorType = CursorType.MODIFY;
            } else {
                cursorType = CursorType.CREATE;
            }
        }
    }

    @Override
    public void onSpaceKeyDown(boolean down) {
        if (down && terrainPosition != null && terrainPositionListener != null) {
            terrainPositionListener.accept(terrainPosition);
        }
    }

    public void activate() {
        if (active) {
            return;
        }
        active = true;
        cursor = setupCursor();
        modifiedSlopes = setupModifiedSlopes();
        modifiedTerrainObjects = setupModifiedTerrainObjects();
        terrainObjectModelMatrices = setupModelMatrices();
        renderService.addRenderTask(terrainEditorRenderTask, "Terrain Editor");
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
        throw new UnsupportedOperationException("... TODO ...");
        // TODO return gameUiControl.getColdGameUiControlConfig().getStaticGameConfig().getPlanetConfig().getTerrainSlopePositions().stream().map(ModifiedSlope::new).collect(Collectors.toList());
    }

    private Collection<ModifiedTerrainObject> setupModifiedTerrainObjects() {
        throw new UnsupportedOperationException("... TODO ...");
        // TODO return gameUiControl.getColdGameUiControlConfig().getStaticGameConfig().getPlanetConfig().getTerrainObjectPositions()
        // TODO         .stream().map(terrainObjectPosition -> new ModifiedTerrainObject(terrainObjectPosition, terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId()).getRadius())).collect(Collectors.toList());
    }

    private List<ModelMatrices> setupModelMatrices() {
        return modifiedTerrainObjects.stream().filter(ModifiedTerrainObject::isNotDeleted).map(modifiedTerrainObject -> modifiedTerrainObject.createModelMatrices(nativeMatrixFactory)).collect(Collectors.toList());
    }

    public void sculpt() {
        List<TerrainSlopePosition> terrainSlopePositions = modifiedSlopes.stream().filter(modifiedSlope -> !modifiedSlope.isEmpty()).map(ModifiedSlope::createTerrainSlopePositionNoId).collect(Collectors.toList());
        List<TerrainObjectPosition> terrainObjectPositions = modifiedTerrainObjects.stream().filter(ModifiedTerrainObject::isNotDeleted).map(ModifiedTerrainObject::createTerrainObjectPositionNoId).collect(Collectors.toList());
        gameEngineControl.overrideTerrain4Editor(terrainSlopePositions, terrainObjectPositions);
        terrainUiService.clearTerrainTilesForEditor();
    }

    public void save() {
        saveSlope();
        saveTerrainObjects();
    }

    private void saveSlope() {
        List<TerrainSlopePosition> createdSlopes = new ArrayList<>();
        List<TerrainSlopePosition> updatedSlopes = new ArrayList<>();
        List<Integer> deletedSlopeIds = new ArrayList<>();
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            if (modifiedSlope.isCreated()) {
                if (!modifiedSlope.isEmpty()) {
                    createdSlopes.add(modifiedSlope.createTerrainSlopePositionNoId());
                }
            } else {
                if (modifiedSlope.isEmpty()) {
                    deletedSlopeIds.add(modifiedSlope.getOriginalId());
                } else if (modifiedSlope.isDirty()) {
                    updatedSlopes.add(modifiedSlope.createTerrainSlopePosition());
                }
            }
        }
        modifiedSlopes.clear();

        if (!createdSlopes.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Slopes Created"), (message, throwable) -> {
                logger.log(Level.SEVERE, "createTerrainSlopePositions failed: " + message, throwable);
                return false;
            }).createTerrainSlopePositions(getPlanetId(), createdSlopes);
        }
        if (!updatedSlopes.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Slopes Updated"), (message, throwable) -> {
                logger.log(Level.SEVERE, "updateTerrainSlopePositions failed: " + message, throwable);
                return false;
            }).updateTerrainSlopePositions(getPlanetId(), updatedSlopes);
        }
        if (!deletedSlopeIds.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Slopes Deleted"), (message, throwable) -> {
                logger.log(Level.SEVERE, "deleteTerrainSlopePositionIds failed: " + message, throwable);
                return false;
            }).deleteTerrainSlopePositionIds(getPlanetId(), deletedSlopeIds);
        }
    }

    private void saveTerrainObjects() {
        List<TerrainObjectPosition> createdTerrainObjects = new ArrayList<>();
        List<TerrainObjectPosition> updatedTerrainObjects = new ArrayList<>();
        List<Integer> deletedTerrainObjectsIds = new ArrayList<>();
        for (ModifiedTerrainObject modifiedTerrainObject : modifiedTerrainObjects) {
            if (modifiedTerrainObject.isCreated()) {
                if (modifiedTerrainObject.isNotDeleted()) {
                    createdTerrainObjects.add(modifiedTerrainObject.createTerrainObjectPositionNoId());
                }
            } else {
                if (!modifiedTerrainObject.isNotDeleted()) {
                    deletedTerrainObjectsIds.add(modifiedTerrainObject.getOriginalId());
                } else if (modifiedTerrainObject.isDirty()) {
                    updatedTerrainObjects.add(modifiedTerrainObject.createTerrainObjectPosition());
                }
            }
        }
        modifiedTerrainObjects.clear();

        if (!createdTerrainObjects.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Object Created"), (message, throwable) -> {
                logger.log(Level.SEVERE, "createTerrainObjectPositions failed: " + message, throwable);
                return false;
            }).createTerrainObjectPositions(getPlanetId(), createdTerrainObjects);
        }
        if (!updatedTerrainObjects.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Object Updated"), (message, throwable) -> {
                logger.log(Level.SEVERE, "updateTerrainObjectPositions failed: " + message, throwable);
                return false;
            }).updateTerrainObjectPositions(getPlanetId(), updatedTerrainObjects);
        }
        if (!deletedTerrainObjectsIds.isEmpty()) {
            planetEditorServiceCaller.call(ignore -> modalDialogManager.showMessageDialog("Terrain Editor", "Terrain Object Deleted"), (message, throwable) -> {
                logger.log(Level.SEVERE, "deleteTerrainObjectPositionIds failed: " + message, throwable);
                return false;
            }).deleteTerrainObjectPositionIds(getPlanetId(), deletedTerrainObjectsIds);
        }
    }

    public void setSlope4New(ObjectNameId slope4New) {
        this.slope4New = slope4New;
    }

    public void setTerrainObject4New(ObjectNameId terrainObject4New) {
        this.terrainObject4New = terrainObject4New;
    }

    public Matrix4 getCursorModelMatrix() {
        return cursorModelMatrix;
    }

    public CursorType getCursorType() {
        return cursorType;
    }

    public boolean isDeletePressed() {
        return deletePressed;
    }

    private ModifiedTerrainObject getTerrainObjectAtTerrain(Vertex terrainPosition) {
        for (ModifiedTerrainObject modifiedTerrainObject : modifiedTerrainObjects) {
            if (modifiedTerrainObject.overlaps(terrainPosition)) {
                return modifiedTerrainObject;
            }
        }
        return null;
    }

    private ModifiedSlope getSlopeAtTerrain(Vertex terrainPosition) {
        Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
        for (ModifiedSlope modifiedSlope : modifiedSlopes) {
            if (modifiedSlope.contains(movedCursor)) {
                return modifiedSlope;
            }
        }
        return null;
    }

    private void dehoverAll() {
        modifiedSlopes.forEach(modifiedSlope -> modifiedSlope.setHover(false));
        modifiedTerrainObjects.forEach(modifiedTerrainObject -> modifiedTerrainObject.setHover(false));
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices() {
        return terrainObjectModelMatrices;
    }

    public void setTerrainObjectRandomZRotation(double terrainObjectRandomZRotation) {
        this.terrainObjectRandomZRotation = terrainObjectRandomZRotation;
    }

    public void setTerrainObjectRandomScale(double terrainObjectRandomScale) {
        this.terrainObjectRandomScale = terrainObjectRandomScale;
    }

    public double getTerrainObjectRandomZRotation() {
        return terrainObjectRandomZRotation;
    }

    public double getTerrainObjectRandomScale() {
        return terrainObjectRandomScale;
    }

    public boolean isCursorVisible() {
        if (hoverTerrainObject != null) {
            return false;
        }
        if (newSlopeMode) {
            return !deletePressed || hoverSlope != null;
        } else {
            return hoverSlope != null;
        }
    }

    public void toggleCreationMode() {
        newSlopeMode = !newSlopeMode;
        modifyingTerrainObject = null;
    }

    public String getCreationModeText() {
        if (newSlopeMode) {
            return "Slope";
        } else {
            return "Terrain Object";
        }
    }

    public void setTerrainPositionListener(Consumer<Vertex> terrainPositionListener) {
        this.terrainPositionListener = terrainPositionListener;
    }

    private int getPlanetId() {
        throw new UnsupportedOperationException("... TODO ...");
        // TODO return gameUiControl.getColdGameUiControlConfig().getStaticGameConfig().getPlanetConfig().getPlanetId();
    }
}
