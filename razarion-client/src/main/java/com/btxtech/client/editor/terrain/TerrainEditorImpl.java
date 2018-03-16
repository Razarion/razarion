package com.btxtech.client.editor.terrain;

import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.dialog.framework.ModalDialogPanel;
import com.btxtech.client.editor.terrain.renderer.TerrainEditorRenderTask;
import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.datatypes.Matrix4;
import com.btxtech.shared.datatypes.Polygon2D;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainEditorLoad;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.dto.TerrainSlopePosition;
import com.btxtech.shared.gameengine.TerrainTypeService;
import com.btxtech.shared.gameengine.datatypes.config.PlanetConfig;
import com.btxtech.shared.gameengine.planet.terrain.TerrainUtil;
import com.btxtech.shared.nativejs.NativeMatrixFactory;
import com.btxtech.shared.rest.PlanetEditorProvider;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.EditorKeyboardListener;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.RenderService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2016.
 */
@ApplicationScoped
public class TerrainEditorImpl implements EditorMouseListener, EditorKeyboardListener {
    public enum CursorType {
        CREATE,
        MODIFY,
        REMOVE_MODE
    }

    // private Logger logger = Logger.getLogger(TerrainEditorImpl.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
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
    private boolean active;
    private boolean slopeMode = true;
    private boolean drivewayMode;
    private Polygon2D cursor;
    private double cursorRadius = 10;
    private int cursorCorners = 20;
    private ObjectNameId slope4New;
    private boolean invertedSlope;
    private ObjectNameId terrainObject4New;
    private ObjectNameId driveway4New;
    private ModifiedSlope hoverSlope;
    private ModifiedTerrainObject hoverTerrainObject;
    private ModifiedTerrainObject modifyingTerrainObject;
    private CursorType cursorType = CursorType.CREATE;
    private ModifiedSlopeContainer modifiedSlopeContainer;
    private Collection<ModifiedTerrainObject> modifiedTerrainObjects;
    private boolean deletePressed;
    private boolean shiftPressed;
    private boolean insertPressed;
    private Matrix4 cursorModelMatrix = Matrix4.createIdentity();
    private Vertex terrainPosition;
    private List<ModelMatrices> terrainObjectModelMatrices;
    private double terrainObjectRandomZRotation = Math.toDegrees(180);
    private double terrainObjectRandomScale = 1.5;
    private Consumer<Vertex> terrainPositionListener;
    private ModalDialogPanel saveDialog;

    @Override
    public void onMouseMove(Vertex terrainPosition, boolean primaryButtonDown) {
        // Cursor
        this.terrainPosition = terrainPosition;

        if (slopeMode) {
            cursorModelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());
            dehoverAll();
            Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
            hoverSlope = modifiedSlopeContainer.getPolygonAt(movedCursor);
            if (hoverSlope != null) {
                hoverSlope.setHover(true);
                if (deletePressed) {
                    cursorType = CursorType.REMOVE_MODE;
                } else {
                    cursorType = CursorType.MODIFY;
                }
                if (primaryButtonDown) {
                    editSlope(terrainPosition);
                }
            } else {
                cursorType = CursorType.CREATE;
            }
        } else {
            if (modifyingTerrainObject != null) {
                modifyingTerrainObject.setNewPosition(terrainPosition, nativeMatrixFactory);
                terrainObjectModelMatrices = setupModelMatrices();
            } else {
                dehoverAll();
                hoverTerrainObject = getTerrainObjectAtTerrain(terrainPosition);
                if (hoverTerrainObject != null) {
                    hoverTerrainObject.setHover(true);
                    hoverSlope = null;

                }
            }
        }
    }

    @Override
    public void onMouseDown(Vertex terrainPosition) {
        if (slopeMode) {
            if (hoverSlope != null) {
                editSlope(terrainPosition);
            } else if (insertPressed) {
                ModifiedSlope parentSlope = hoverSlope = modifiedSlopeContainer.getPolygonAt(terrainPosition.toXY());
                Integer editorParentId = null;
                if (parentSlope != null) {
                    if (parentSlope.isCreated()) {
                        throw new IllegalArgumentException("TerrainEditorImpl.onMouseDown() Can not create child slope while parent is not saved.");
                    }
                    editorParentId = parentSlope.getOriginalId();
                }
                ModifiedSlope slopePosition = new ModifiedSlope(slope4New.getId(), invertedSlope, editorParentId, cursor.translate(terrainPosition.toXY()));
                modifiedSlopeContainer.add(slopePosition);
                terrainEditorRenderTask.newSlope(slopePosition);
            }
        } else {
            if (hoverTerrainObject != null) {
                if (deletePressed) {
                    hoverTerrainObject.setDeleted();
                    hoverTerrainObject = null;
                    modifyingTerrainObject = null;
                    terrainObjectModelMatrices = setupModelMatrices();
                } else {
                    modifyingTerrainObject = hoverTerrainObject;
                }
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

    private void editSlope(Vertex terrainPosition) {
        Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
        if (deletePressed) {
            if (drivewayMode) {
                hoverSlope.decreaseDriveway(movedCursor);
                terrainEditorRenderTask.updateSlope(hoverSlope);
            } else {
                if (shiftPressed) {
                    if (hoverSlope.isParent()) {
                        modalDialogManager.showMessageDialog("Delete", "Delete all child slopes first");
                    } else {
                        modifiedSlopeContainer.remove(hoverSlope);
                        terrainEditorRenderTask.removeSlope(hoverSlope);
                        hoverSlope = null;
                    }
                } else {
                    Polygon2D newPolygon = hoverSlope.remove(movedCursor);
                    if (newPolygon != null) {
                        terrainEditorRenderTask.updateSlope(hoverSlope);
                        modifiedSlopeContainer.update(hoverSlope);
                    } else {
                        terrainEditorRenderTask.removeSlope(hoverSlope);
                        modifiedSlopeContainer.remove(hoverSlope);
                    }
                }
            }
        } else {
            if (drivewayMode) {
                hoverSlope.increaseDriveway(movedCursor, terrainTypeService.getDrivewayConfig(driveway4New.getId()));
            } else {
                hoverSlope.combine(movedCursor);
                modifiedSlopeContainer.update(hoverSlope);
            }
            terrainEditorRenderTask.updateSlope(hoverSlope);
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
            }
        }
    }

    @Override
    public void onShiftKeyDown(boolean down) {
        shiftPressed = down;
    }

    @Override
    public void onInsertKeyDown(boolean down) {
        insertPressed = down;
        if (down) {
            if (hoverSlope != null) {
                cursorType = CursorType.CREATE;
            }
        } else {
            if (hoverSlope != null) {
                cursorType = CursorType.MODIFY;
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
        renderService.addRenderTask(terrainEditorRenderTask, "Terrain Editor");
        terrainMouseHandler.setEditorMouseListener(this);
        keyboardEventHandler.setEditorKeyboardListener(this);
        modifiedSlopeContainer = new ModifiedSlopeContainer(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        modifiedTerrainObjects = new ArrayList<>();
        loadFromServer();
    }

    private void loadFromServer() {
        planetEditorServiceCaller.call((RemoteCallback<TerrainEditorLoad>) terrainEditorLoad -> {
            hoverSlope = null;
            hoverTerrainObject = null;
            terrainEditorRenderTask.deactivate();
            modifiedSlopeContainer.setPolygons(setupModifiedSlope(terrainEditorLoad.getSlopes()));
            modifiedTerrainObjects = terrainEditorLoad.getTerrainObjects().stream().map(terrainObjectPosition -> new ModifiedTerrainObject(terrainObjectPosition, terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectId()).getRadius())).collect(Collectors.toList());
            terrainObjectModelMatrices = setupModelMatrices();
            terrainEditorRenderTask.activate(cursor, modifiedSlopeContainer.getPolygons());
            if (saveDialog != null) {
                saveDialog.close();
                saveDialog = null;
            }
        }, exceptionHandler.restErrorHandler("readTerrainSlopePositions failed: ")).readTerrainEditorLoad(getPlanetId());
    }

    private Collection<ModifiedSlope> setupModifiedSlope(List<TerrainSlopePosition> slopes) {
        Collection<ModifiedSlope> modifiedSlopes = new ArrayList<>();
        addModifiedSlope(modifiedSlopes, slopes);
        return modifiedSlopes;
    }

    private void addModifiedSlope(Collection<ModifiedSlope> modifiedSlopes, List<TerrainSlopePosition> slopes) {
        for (TerrainSlopePosition slope : slopes) {
            modifiedSlopes.add(new ModifiedSlope(slope));
            if (slope.getChildren() != null) {
                addModifiedSlope(modifiedSlopes, slope.getChildren());
            }
        }
    }

    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        cursor = null;
        modifiedSlopeContainer = null;
        renderService.removeRenderTask(terrainEditorRenderTask);
        terrainMouseHandler.setEditorMouseListener(null);
        keyboardEventHandler.setEditorKeyboardListener(null);
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

    private List<ModelMatrices> setupModelMatrices() {
        return modifiedTerrainObjects.stream().filter(ModifiedTerrainObject::isNotDeleted).map(modifiedTerrainObject -> modifiedTerrainObject.createModelMatrices(nativeMatrixFactory)).collect(Collectors.toList());
    }

    public void restartPlanetButton() {
        modalDialogManager.showQuestionDialog("Restart planet", "Really restart the planet? Close all current connections.", () -> planetEditorServiceCaller.call(ignore -> {
        }, exceptionHandler.restErrorHandler("PlanetEditorProvider.restartPlanetWarm() failed: ")).restartPlanetWarm(getPlanetId()), () -> {
        });
    }

    public void save() {
        TerrainEditorUpdate terrainEditorUpdate = new TerrainEditorUpdate();
        setupChangedSlopes(terrainEditorUpdate);
        setupChangedTerrainObjects(terrainEditorUpdate);

        if (!terrainEditorUpdate.hasAnyChanged()) {
            return;
        }
        modalDialogManager.showMessageNoClosableDialog("Save", "Please wait while saving terrain", modalDialogPanel -> this.saveDialog = modalDialogPanel);
        planetEditorServiceCaller.call(ignore -> loadFromServer(), (message, throwable) -> {
            if (saveDialog != null) {
                saveDialog.close();
                saveDialog = null;
            }
            modalDialogManager.showMessageDialog("Save failed", "Save terrain failed. message: " + message + " throwable: " + throwable);
            return false;
        }).updateTerrain(getPlanetId(), terrainEditorUpdate);
    }

    private void setupChangedSlopes(TerrainEditorUpdate terrainEditorUpdate) {
        List<TerrainSlopePosition> createdSlopes = new ArrayList<>();
        List<TerrainSlopePosition> updatedSlopes = new ArrayList<>();
        for (ModifiedSlope modifiedSlope : modifiedSlopeContainer.getPolygons()) {
            if (modifiedSlope.isCreated()) {
                if (!modifiedSlope.isEmpty()) {
                    createdSlopes.add(modifiedSlope.createTerrainSlopePositionNoId());
                }
            } else if (modifiedSlope.isDirty()) {
                updatedSlopes.add(modifiedSlope.createTerrainSlopePosition());
            }
        }
        terrainEditorUpdate.setCreatedSlopes(createdSlopes);
        terrainEditorUpdate.setUpdatedSlopes(updatedSlopes);
        terrainEditorUpdate.setDeletedSlopeIds(modifiedSlopeContainer.getAndClearDeletedSlopeIds());
    }

    private void setupChangedTerrainObjects(TerrainEditorUpdate terrainEditorUpdate) {
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
        terrainEditorUpdate.setCreatedTerrainObjects(createdTerrainObjects);
        terrainEditorUpdate.setUpdatedTerrainObjects(updatedTerrainObjects);
        terrainEditorUpdate.setDeletedTerrainObjectsIds(deletedTerrainObjectsIds);
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

    private void dehoverAll() {
        modifiedSlopeContainer.getPolygons().forEach(modifiedSlope -> modifiedSlope.setHover(false));
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
        if (slopeMode) {
            if (insertPressed) {
                return hoverSlope == null;
            }
            return hoverSlope != null;
        } else {
            return hoverSlope != null;
        }
    }

    public void setSlopeMode(boolean slopeMode) {
        if (this.slopeMode == slopeMode) {
            return;
        }
        this.slopeMode = slopeMode;
        hoverSlope = null;
        modifyingTerrainObject = null;
        hoverTerrainObject = null;
    }

    public boolean getCreationMode() {
        return this.slopeMode;
    }

    public void setTerrainPositionListener(Consumer<Vertex> terrainPositionListener) {
        this.terrainPositionListener = terrainPositionListener;
    }

    public int getPlanetId() {
        return getPlanetConfig().getPlanetId();
    }

    public PlanetConfig getPlanetConfig() {
        return gameUiControl.getPlanetConfig();
    }

    public void setDrivewayModeChanged(boolean drivewayMode) {
        this.drivewayMode = drivewayMode;
    }

    public boolean isDrivewayMode() {
        return drivewayMode;
    }

    public void setDriveway4New(ObjectNameId driveway4New) {
        this.driveway4New = driveway4New;
    }

    public void saveMiniMapImage(String dataUrl) {
        planetEditorServiceCaller.call(ignore -> {
        }, exceptionHandler.restErrorHandler("updateMiniMapImage failed: ")).updateMiniMapImage(getPlanetId(), dataUrl);
    }

    public boolean isInvertedSlope() {
        return invertedSlope;
    }

    public void setInvertedSlope(boolean invertedSlope) {
        this.invertedSlope = invertedSlope;
    }
}
