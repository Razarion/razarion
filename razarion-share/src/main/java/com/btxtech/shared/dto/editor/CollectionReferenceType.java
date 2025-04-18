package com.btxtech.shared.dto.editor;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.rest.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum CollectionReferenceType {
    PLANET(PlanetEditorController.class, "Planet", CommonUrl.PLANET_EDITOR_PATH),
    GROUND(GroundEditorController.class, "Ground", CommonUrl.GROUND_EDITOR_PATH),
    GAME_UI_CONTEXT(GameUiContextEditorController.class, "Game Ui Context", CommonUrl.GAME_UI_CONTEXT_EDITOR_PATH),
    TERRAIN_OBJECT(TerrainObjectEditorController.class, "Terrain Object", CommonUrl.TERRAIN_OBJECT_EDITOR_PATH),
    BASE_ITEM(BaseItemTypeEditorController.class, "Base Item", CommonUrl.BASE_ITEM_TYPE_EDITOR_PATH),
    RESOURCE_ITEM(ResourceItemTypeEditorController.class, "Resource Item", CommonUrl.RESOURCE_ITEM_TYPE_EDITOR_PATH),
    SERVER_GAME_ENGINE(ServerGameEngineEditorController.class, "Server Game Engine", CommonUrl.SERVER_GAME_ENGINE_EDITOR_PATH),
    IMAGE(null, "Image", null);

    private static Map<String, CollectionReferenceType> collectionName2Type;
    private final Class<? extends CrudController<? extends Config>> crudControllerClass;
    private final String collectionName;
    private final String path;

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
