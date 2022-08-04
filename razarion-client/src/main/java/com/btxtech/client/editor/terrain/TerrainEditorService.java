package com.btxtech.client.editor.terrain;

import com.btxtech.client.KeyboardEventHandler;
import com.btxtech.client.dialog.framework.ClientModalDialogManagerImpl;
import com.btxtech.client.editor.terrain.renderer.TerrainEditorRenderTaskRunner;
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
import com.btxtech.shared.rest.DrivewayEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
import com.btxtech.shared.rest.TerrainEditorController;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.EditorKeyboardListener;
import com.btxtech.uiservice.EditorMouseListener;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.datatypes.ModelMatrices;
import com.btxtech.uiservice.mouse.TerrainMouseHandler;
import com.btxtech.uiservice.renderer.RenderService;
import elemental2.promise.Promise;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Beat
 * 05.05.2016.
 */
@JsType
@ApplicationScoped
public class TerrainEditorService implements EditorMouseListener, EditorKeyboardListener {
    // private Logger logger = Logger.getLogger(TerrainEditorImpl.class.getName());
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<TerrainEditorController> terrainEditorController;
    @Inject
    private RenderService renderService;
    @Inject
    private TerrainMouseHandler terrainMouseHandler;
    @Inject
    private KeyboardEventHandler keyboardEventHandler;
    @Inject
    private TerrainEditorRenderTaskRunner terrainEditorRenderTask;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private ClientModalDialogManagerImpl modalDialogManager;
    @Inject
    private TerrainTypeService terrainTypeService;
    @Inject
    private NativeMatrixFactory nativeMatrixFactory;
    @Inject
    private Caller<SlopeEditorController> slopeEditorController;
    @Inject
    private Caller<DrivewayEditorController> drivewayEditorController;
    @Inject
    private Caller<TerrainObjectEditorController> terrainObjectEditorController;
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
    private EditorSlopeWrapper hoverSlope;
    private EditorTerrainObjectWrapper hoverTerrainObject;
    private EditorTerrainObjectWrapper modifyingTerrainObject;
    private CursorType cursorType = CursorType.CREATE;
    private EditorSlopeWrapperContainer editorSlopeWrapperContainer;
    private Collection<EditorTerrainObjectWrapper> terrainObjects;
    private boolean deletePressed;
    private boolean shiftPressed;
    private boolean insertPressed;
    private Matrix4 cursorModelMatrix = Matrix4.createIdentity();
    private Vertex terrainPosition;
    private List<ModelMatrices> terrainObjectModelMatrices;
    private double terrainObjectRandomZRotation = Math.toDegrees(180);
    private double terrainObjectRandomScale = 1.5;
    private Consumer<Vertex> terrainPositionListener;

    @Override
    @JsIgnore
    public void onMouseMove(Vertex terrainPosition, boolean primaryButtonDown) {
        // Cursor
        this.terrainPosition = terrainPosition;

        if (slopeMode) {
            cursorModelMatrix = Matrix4.createTranslation(terrainPosition.getX(), terrainPosition.getY(), terrainPosition.getZ());
            dehoverAll();
            Polygon2D movedCursor = cursor.translate(terrainPosition.toXY());
            hoverSlope = editorSlopeWrapperContainer.getPolygonAt(movedCursor);
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
    @JsIgnore
    public void onMouseDown(Vertex terrainPosition) {
        if (slopeMode) {
            if (hoverSlope != null) {
                editSlope(terrainPosition);
            } else if (insertPressed) {
                EditorSlopeWrapper parentSlope = hoverSlope = editorSlopeWrapperContainer.getPolygonAt(terrainPosition.toXY());
                Integer editorParentId = null;
                if (parentSlope != null) {
                    if (parentSlope.isCreated()) {
                        throw new IllegalArgumentException("TerrainEditorImpl.onMouseDown() Can not create child slope while parent is not saved.");
                    }
                    editorParentId = parentSlope.getOriginalId();
                }
                EditorSlopeWrapper slopePosition = new EditorSlopeWrapper(slope4New.getId(), invertedSlope, editorParentId, cursor.translate(terrainPosition.toXY()));
                editorSlopeWrapperContainer.add(slopePosition);
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
                EditorTerrainObjectWrapper objectPosition = new EditorTerrainObjectWrapper(terrainObject4New.getId(), terrainPosition.toXY(), scale, rotationZ, radius);
                modifyingTerrainObject = objectPosition;
                hoverTerrainObject = objectPosition;
                terrainObjects.add(objectPosition);
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
                        editorSlopeWrapperContainer.remove(hoverSlope);
                        terrainEditorRenderTask.removeSlope(hoverSlope);
                        hoverSlope = null;
                    }
                } else {
                    Polygon2D newPolygon = hoverSlope.remove(movedCursor);
                    if (newPolygon != null) {
                        terrainEditorRenderTask.updateSlope(hoverSlope);
                        editorSlopeWrapperContainer.update(hoverSlope);
                    } else {
                        terrainEditorRenderTask.removeSlope(hoverSlope);
                        editorSlopeWrapperContainer.remove(hoverSlope);
                    }
                }
            }
        } else {
            if (drivewayMode) {
                hoverSlope.increaseDriveway(movedCursor, terrainTypeService.getDrivewayConfig(driveway4New.getId()));
            } else {
                hoverSlope.combine(movedCursor);
                editorSlopeWrapperContainer.update(hoverSlope);
            }
            terrainEditorRenderTask.updateSlope(hoverSlope);
        }
    }

    @Override
    @JsIgnore
    public void onMouseUp() {
        modifyingTerrainObject = null;
    }

    @Override
    @JsIgnore
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
    @JsIgnore
    public void onShiftKeyDown(boolean down) {
        shiftPressed = down;
    }

    @Override
    @JsIgnore
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
    @JsIgnore
    public void onSpaceKeyDown(boolean down) {
        if (down && terrainPosition != null && terrainPositionListener != null) {
            terrainPositionListener.accept(terrainPosition);
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void activate() {
        if (active) {
            return;
        }
        active = true;
        cursor = setupCursor();
        renderService.addRenderTaskRunner(terrainEditorRenderTask, "Terrain Editor");
        terrainMouseHandler.setEditorMouseListener(this);
        keyboardEventHandler.setEditorKeyboardListener(this);
        editorSlopeWrapperContainer = new EditorSlopeWrapperContainer(TerrainUtil.TERRAIN_NODE_ABSOLUTE_LENGTH);
        terrainObjects = new ArrayList<>();
        loadFromServer();
    }

    private void loadFromServer() {
        terrainEditorController.call((RemoteCallback<TerrainEditorLoad>) terrainEditorLoad -> {
            hoverSlope = null;
            hoverTerrainObject = null;
            terrainEditorRenderTask.deactivate();
            editorSlopeWrapperContainer.setPolygons(setupEditorSlopeWrappers(terrainEditorLoad.getSlopes()));
            terrainObjects = terrainEditorLoad.getTerrainObjects()
                    .stream()
                    .map(terrainObjectPosition -> new EditorTerrainObjectWrapper(terrainObjectPosition,
                            terrainTypeService.getTerrainObjectConfig(terrainObjectPosition.getTerrainObjectConfigId()).getRadius()))
                    .collect(Collectors.toList());
            terrainObjectModelMatrices = setupModelMatrices();
            terrainEditorRenderTask.activate(cursor, editorSlopeWrapperContainer.getPolygons(), slopeMode);
        }, exceptionHandler.restErrorHandler("readTerrainSlopePositions failed: ")).readTerrainEditorLoad(getPlanetId());
    }

    private Collection<EditorSlopeWrapper> setupEditorSlopeWrappers(List<TerrainSlopePosition> slopes) {
        Collection<EditorSlopeWrapper> result = new ArrayList<>();
        appendRecursiveEditorSlopeWrappers(result, slopes);
        return result;
    }

    private void appendRecursiveEditorSlopeWrappers(Collection<EditorSlopeWrapper> result, List<TerrainSlopePosition> slopes) {
        for (TerrainSlopePosition slope : slopes) {
            result.add(new EditorSlopeWrapper(slope));
            if (slope.getChildren() != null) {
                appendRecursiveEditorSlopeWrappers(result, slope.getChildren());
            }
        }
    }

    @SuppressWarnings("unused") // Called by Angular
    public void deactivate() {
        if (!active) {
            return;
        }
        active = false;
        cursor = null;
        editorSlopeWrapperContainer = null;
        renderService.removeRenderTaskRunner(terrainEditorRenderTask);
        terrainMouseHandler.setEditorMouseListener(null);
        keyboardEventHandler.setEditorKeyboardListener(null);
        terrainEditorRenderTask.deactivate();
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getCursorRadius() {
        return cursorRadius;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setCursorRadius(double cursorRadius) {
        this.cursorRadius = cursorRadius;
        onCursorChanged();
    }

    @SuppressWarnings("unused") // Called by Angular
    public int getCursorCorners() {
        return cursorCorners;
    }

    @SuppressWarnings("unused") // Called by Angular
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
        return terrainObjects
                .stream()
                .filter(EditorTerrainObjectWrapper::isNotDeleted)
                .map(modifiedTerrainObject -> modifiedTerrainObject.createModelMatrices(nativeMatrixFactory))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<String> save(TerrainObjectPosition[] createdTerrainObjects) {
        TerrainEditorUpdate terrainEditorUpdate = new TerrainEditorUpdate();
        terrainEditorUpdate.setCreatedSlopes(new ArrayList<>());
        terrainEditorUpdate.setUpdatedSlopes(new ArrayList<>());
        terrainEditorUpdate.setDeletedSlopeIds(new ArrayList<>());

        // setupChangedSlopes(terrainEditorUpdate);
        terrainEditorUpdate.setCreatedTerrainObjects(Arrays.asList(createdTerrainObjects));
        terrainEditorUpdate.setUpdatedTerrainObjects(new ArrayList<>());
        terrainEditorUpdate.setDeletedTerrainObjectsIds(new ArrayList<>());

        if (!terrainEditorUpdate.hasAnyChanged()) {
            return new Promise<>((resolve, reject) -> resolve.onInvoke("Terrain not changed. Save not needed."));
        }
        return new Promise<>((resolve, reject) -> terrainEditorController.call(ignore -> {
            loadFromServer();
            resolve.onInvoke("Terrain saved");
        }, (message, throwable) -> {
            exceptionHandler.handleException(message.toString(), throwable);
            reject.onInvoke(message.toString() + throwable);
            return false;
        }).updateTerrain(getPlanetId(), terrainEditorUpdate));
    }

    private void setupChangedSlopes(TerrainEditorUpdate terrainEditorUpdate) {
        List<TerrainSlopePosition> createdSlopes = new ArrayList<>();
        List<TerrainSlopePosition> updatedSlopes = new ArrayList<>();
        for (EditorSlopeWrapper modifiedSlope : editorSlopeWrapperContainer.getPolygons()) {
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
        terrainEditorUpdate.setDeletedSlopeIds(editorSlopeWrapperContainer.getAndClearDeletedSlopeIds());
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setSlope4New(ObjectNameId slope4New) {
        this.slope4New = slope4New;
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> getAllSlopes() {
        return new Promise<>((resolve, reject) -> slopeEditorController.call(
                (RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> resolve.onInvoke(objectNameIds.toArray(new ObjectNameId[0])),
                exceptionHandler.restErrorHandler("SlopeEditorController.getObjectNameIds() failed: ")).getObjectNameIds());
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> getAllDriveways() {
        return new Promise<>((resolve, reject) -> drivewayEditorController.call(
                (RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> resolve.onInvoke(objectNameIds.toArray(new ObjectNameId[0])),
                exceptionHandler.restErrorHandler("DrivewayEditorController.getObjectNameIds() failed: ")).getObjectNameIds());
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> getAllTerrainObjects() {
        return new Promise<>((resolve, reject) -> terrainObjectEditorController.call(
                (RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> resolve.onInvoke(objectNameIds.toArray(new ObjectNameId[0])),
                exceptionHandler.restErrorHandler("TerrainObjectEditorController.getObjectNameIds() failed: ")).getObjectNameIds());
    }

    @SuppressWarnings("unused") // Called by Angular
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

    private EditorTerrainObjectWrapper getTerrainObjectAtTerrain(Vertex terrainPosition) {
        return terrainObjects
                .stream()
                .filter(modifiedTerrainObject -> modifiedTerrainObject.overlaps(terrainPosition))
                .findFirst()
                .orElse(null);
    }

    private void dehoverAll() {
        editorSlopeWrapperContainer.getPolygons().forEach(modifiedSlope -> modifiedSlope.setHover(false));
        terrainObjects.forEach(modifiedTerrainObject -> modifiedTerrainObject.setHover(false));
    }

    public List<ModelMatrices> provideTerrainObjectModelMatrices() {
        return terrainObjectModelMatrices;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getTerrainObjectRandomZRotation() {
        return terrainObjectRandomZRotation;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setTerrainObjectRandomZRotation(double terrainObjectRandomZRotation) {
        this.terrainObjectRandomZRotation = terrainObjectRandomZRotation;
    }

    @SuppressWarnings("unused") // Called by Angular
    public double getTerrainObjectRandomScale() {
        return terrainObjectRandomScale;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setTerrainObjectRandomScale(double terrainObjectRandomScale) {
        this.terrainObjectRandomScale = terrainObjectRandomScale;
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isSlopeMode() {
        return this.slopeMode;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setSlopeMode(boolean slopeMode) {
        if (this.slopeMode == slopeMode) {
            return;
        }
        this.slopeMode = slopeMode;
        hoverSlope = null;
        modifyingTerrainObject = null;
        hoverTerrainObject = null;
        terrainEditorRenderTask.setSlopeMode(slopeMode);
    }

    // TODO -> call from angular?
    @JsIgnore
    public void setTerrainPositionListener(Consumer<Vertex> terrainPositionListener) {
        this.terrainPositionListener = terrainPositionListener;
    }

    // TODO -> call from angular?
    @JsIgnore
    public int getPlanetId() {
        return getPlanetConfig().getId();
    }

    // TODO -> call from angular?
    @JsIgnore
    public PlanetConfig getPlanetConfig() {
        return gameUiControl.getPlanetConfig();
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isDrivewayMode() {
        return drivewayMode;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setDrivewayMode(boolean drivewayMode) {
        this.drivewayMode = drivewayMode;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setDriveway4New(ObjectNameId driveway4New) {
        this.driveway4New = driveway4New;
    }

    // TODO -> call from angular?
    @JsIgnore
    public void saveMiniMapImage(String dataUrl) {
        terrainEditorController.call(ignore -> {
        }, exceptionHandler.restErrorHandler("updateMiniMapImage failed: ")).updateMiniMapImage(getPlanetId(), dataUrl);
    }

    @SuppressWarnings("unused") // Called by Angular
    public boolean isInvertedSlope() {
        return invertedSlope;
    }

    @SuppressWarnings("unused") // Called by Angular
    public void setInvertedSlope(boolean invertedSlope) {
        this.invertedSlope = invertedSlope;
    }

    public enum CursorType {
        CREATE,
        MODIFY,
        REMOVE_MODE
    }
}
