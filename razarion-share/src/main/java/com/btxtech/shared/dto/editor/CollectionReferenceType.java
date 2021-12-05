package com.btxtech.shared.dto.editor;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.rest.AssetEditorController;
import com.btxtech.shared.rest.BaseItemTypeEditorController;
import com.btxtech.shared.rest.CrudController;
import com.btxtech.shared.rest.DrivewayEditorController;
import com.btxtech.shared.rest.GameUiContextEditorController;
import com.btxtech.shared.rest.GroundEditorController;
import com.btxtech.shared.rest.LevelEditorController;
import com.btxtech.shared.rest.MeshContainerEditorController;
import com.btxtech.shared.rest.ParticleEmitterSequenceEditorController;
import com.btxtech.shared.rest.ParticleShapeEditorController;
import com.btxtech.shared.rest.PlanetEditorController;
import com.btxtech.shared.rest.ResourceItemTypeEditorController;
import com.btxtech.shared.rest.ServerGameEngineEditorController;
import com.btxtech.shared.rest.Shape3DEditorController;
import com.btxtech.shared.rest.SlopeEditorController;
import com.btxtech.shared.rest.TerrainObjectEditorController;
import com.btxtech.shared.rest.WaterEditorController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public enum CollectionReferenceType {
    LEVEL(LevelEditorController.class, "Level"),
    PLANET(PlanetEditorController.class, "Planet"),
    GROUND(GroundEditorController.class, "Ground"),
    SLOPE(SlopeEditorController.class, "Slope"),
    DRIVEWAY(DrivewayEditorController.class, "Driveway"),
    WATER(WaterEditorController.class, "Water"),
    GAME_UI_CONTEXT(GameUiContextEditorController.class, "Game Ui Context"),
    SHAPE_3D(Shape3DEditorController.class, "Shape 3D"),
    MESH_CONTAINER(MeshContainerEditorController.class, "Mesh Container"),
    ASSET(AssetEditorController.class, "Asset"),
    TERRAIN_OBJECT(TerrainObjectEditorController.class, "Terrain Object"),
    BASE_ITEM(BaseItemTypeEditorController.class, "Base Item"),
    RESOURCE_ITEM(ResourceItemTypeEditorController.class, "Resource Item"),
    PARTICLE_SHAPE(ParticleShapeEditorController.class, "Particle Shape"),
    PARTICLE_EMITTER_SEQUENCE(ParticleEmitterSequenceEditorController.class, "Particle Emitter Sequence"),
    SERVER_GAME_ENGINE(ServerGameEngineEditorController.class, "Server Game Engine"),
    IMAGE(null, "Image");

    private static Map<String, CollectionReferenceType> collectionName2Type;
    private Class<? extends CrudController<? extends Config>> crudControllerClass;
    private String collectionName;

    CollectionReferenceType(Class<? extends CrudController<? extends Config>> crudControllerClass, String collectionName) {
        this.crudControllerClass = crudControllerClass;
        this.collectionName = collectionName;
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

    public Class<? extends CrudController<? extends Config>> getCrudControllerClass() {
        return crudControllerClass;
    }
}
