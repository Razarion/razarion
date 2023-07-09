package com.btxtech.shared.dto.editor;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.rest.AssetEditorController;
import com.btxtech.shared.rest.BaseItemTypeEditorController;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.rest.DrivewayEditorController;
import com.btxtech.shared.rest.GameUiContextEditorController;
import com.btxtech.shared.rest.GroundEditorController;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.rest.MeshContainerEditorController;
import com.btxtech.shared.rest.ParticleSystemEditorController;
import com.btxtech.shared.rest.PlanetEditorController;
import com.btxtech.shared.rest.ResourceItemTypeEditorController;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.rest.ThreeJsModelEditorController;
import com.btxtech.shared.rest.ThreeJsModelPackEditorController;
import com.btxtech.shared.rest.WaterEditorController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum CollectionReferenceType {
    LEVEL(LevelEditorController.class, "Level", CommonUrl.LEVEL_EDITOR_PATH),
    PLANET(PlanetEditorController.class, "Planet", CommonUrl.PLANET_EDITOR_PATH),
    GROUND(GroundEditorController.class, "Ground", CommonUrl.GROUND_EDITOR_PATH),
    SLOPE(SlopeEditorController.class, "Slope", CommonUrl.SLOPE_EDITOR_PATH),
    DRIVEWAY(DrivewayEditorController.class, "Driveway", CommonUrl.DRIVEWAY_EDITOR_PATH),
    WATER(WaterEditorController.class, "Water", CommonUrl.WATER_EDITOR_PATH),
    GAME_UI_CONTEXT(GameUiContextEditorController.class, "Game Ui Context", CommonUrl.GAME_UI_CONTEXT_EDITOR_PATH),
    SHAPE_3D(Shape3DEditorController.class, "Shape 3D", CommonUrl.SHAPE_3D_EDITOR_PATH),
    MESH_CONTAINER(MeshContainerEditorController.class, "Mesh Container", CommonUrl.MESH_CONTAINER_EDITOR_PATH),
    ASSET(AssetEditorController.class, "Asset", CommonUrl.ASSET_EDITOR_PATH),
    TERRAIN_OBJECT(TerrainObjectEditorController.class, "Terrain Object", CommonUrl.TERRAIN_OBJECT_EDITOR_PATH),
    BASE_ITEM(BaseItemTypeEditorController.class, "Base Item", CommonUrl.BASE_ITEM_TYPE_EDITOR_PATH),
    RESOURCE_ITEM(ResourceItemTypeEditorController.class, "Resource Item", CommonUrl.RESOURCE_ITEM_TYPE_EDITOR_PATH),
    SERVER_GAME_ENGINE(ServerGameEngineEditorController.class, "Server Game Engine", CommonUrl.SERVER_GAME_ENGINE_EDITOR_PATH),
    THREE_JS_MODEL(ThreeJsModelEditorController.class, "Three.js Model", CommonUrl.THREE_JS_MODEL_EDITOR_PATH),
    THREE_JS_MODEL_PACK(ThreeJsModelPackEditorController.class, "Three.js Model Pack", CommonUrl.THREE_JS_MODEL_PACK_EDITOR_PATH),
    PARTICLE_SYSTEM(ParticleSystemEditorController.class, "Particle System", CommonUrl.PARTICLE_SYSTEM_EDITOR_PATH),
    IMAGE(null, "Image", null);

    private static Map<String, CollectionReferenceType> collectionName2Type;
    private Class<? extends CrudController<? extends Config>> crudControllerClass;
    private String collectionName;
    private String path;

    CollectionReferenceType(Class<? extends CrudController<? extends Config>> crudControllerClass, String collectionName, String path) {
        this.crudControllerClass = crudControllerClass;
        this.collectionName = collectionName;
        this.path = path;
    }

    public static CollectionReferenceType getType4CollectionName(String collectionName) {
        if (collectionName2Type == null) {
            collectionName2Type = new HashMap<>();
            Arrays.stream(values()).forEach(collectionReferenceType -> collectionName2Type.put(collectionReferenceType.getCollectionName(), collectionReferenceType));
        }
        CollectionReferenceType collectionReferenceType = collectionName2Type.get(collectionName);
        if (collectionReferenceType == null) {
            throw new IllegalArgumentException("No CollectionReferenceType for collection name: " + collectionName);
        }
        return collectionReferenceType;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public String getPath() {
        return path;
    }

    public Class<? extends CrudController<? extends Config>> getCrudControllerClass() {
        return crudControllerClass;
    }
}
