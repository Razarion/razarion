package com.btxtech.client.editor;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.ObjectNameId;
import com.btxtech.shared.dto.TerrainEditorUpdate;
import com.btxtech.shared.dto.TerrainObjectPosition;
import com.btxtech.shared.rest.TerrainEditorController;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.uiservice.control.GameUiControl;
import com.btxtech.uiservice.renderer.BabylonTerrainTile;
import com.btxtech.uiservice.terrain.TerrainUiService;
import com.btxtech.uiservice.terrain.UiTerrainTile;
import elemental2.promise.Promise;
import jsinterop.annotations.JsType;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@JsType
@ApplicationScoped
public class TerrainEditorService {
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;
    @Inject
    private Caller<TerrainEditorController> terrainEditorController;
    @Inject
    private GameUiControl gameUiControl;
    @Inject
    private Caller<TerrainObjectEditorController> terrainObjectEditorController;
    @Inject
    private TerrainUiService terrainUiService;

    @SuppressWarnings("unused") // Called by Angular
    public Promise<String> save(TerrainObjectPosition[] createdTerrainObjects, TerrainObjectPosition[] updatedTerrainObjects) {
        TerrainEditorUpdate terrainEditorUpdate = new TerrainEditorUpdate();
        terrainEditorUpdate.setCreatedTerrainObjects(Arrays.asList(createdTerrainObjects));
        terrainEditorUpdate.setUpdatedTerrainObjects(Arrays.asList(updatedTerrainObjects));
        terrainEditorUpdate.setDeletedTerrainObjectsIds(new ArrayList<>());

        if (!terrainEditorUpdate.hasAnyChanged()) {
            return new Promise<>((resolve, reject) -> resolve.onInvoke("Terrain not changed. Save not needed."));
        }
        return new Promise<>((resolve, reject) -> terrainEditorController.call(ignore -> {
            resolve.onInvoke("Terrain saved");
        }, (message, throwable) -> {
            exceptionHandler.handleException(message.toString(), throwable);
            reject.onInvoke(message.toString() + throwable);
            return false;
        }).updateTerrain(gameUiControl.getPlanetConfig().getId(), terrainEditorUpdate));
    }

    @SuppressWarnings("unused") // Called by Angular
    public Promise<ObjectNameId[]> getAllTerrainObjects() {
        return new Promise<>((resolve, reject) -> terrainObjectEditorController.call(
                (RemoteCallback<Collection<ObjectNameId>>) objectNameIds -> resolve.onInvoke(objectNameIds.toArray(new ObjectNameId[0])),
                exceptionHandler.restErrorHandler("TerrainObjectEditorController.getObjectNameIds() failed: ")).getObjectNameIds());
    }

    @SuppressWarnings("unused") // Called by Angular
    public BabylonTerrainTile[] getAllBabylonTerrainTile() {
        // initialize terrainUiService
        return terrainUiService.getAllUiTerrainTiles()
                .stream()
                .map(UiTerrainTile::getBabylonTerrainTile)
                .toArray(BabylonTerrainTile[]::new);
    }

}
