package com.btxtech.shared.datatypes.shape.config;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.editor.CustomEditor;
import com.btxtech.shared.dto.editor.CustomEditorType;

import java.util.List;

/**
 * Created by Beat
 * 21.08.2016.
 */
public class Shape3DConfig implements Config {
    private int id;
    private String internalName;
    @CustomEditor(CustomEditorType.COLLADA)
    private String colladaString;
    private List<Shape3DElementConfig> shape3DElementConfigs;

    public int getId() {
        return id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    public List<Shape3DElementConfig> getShape3DElementConfigs() {
        return this.shape3DElementConfigs;
    }

    public void setShape3DElementConfigs(List<Shape3DElementConfig> shape3DElementConfigs) {
        this.shape3DElementConfigs = shape3DElementConfigs;
    }

    public Shape3DConfig id(int id) {
        this.id = id;
        return this;
    }

    public Shape3DConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public Shape3DConfig colladaString(String colladaString) {
        setColladaString(colladaString);
        return this;
    }

    public Shape3DConfig shape3DElementConfigs(List<Shape3DElementConfig> shape3DElementConfigs) {
        setShape3DElementConfigs(shape3DElementConfigs);
        return this;
    }

    public VertexContainerMaterialConfig findMaterial(String elementId, String materialId) {
        return shape3DElementConfigs.stream()
                .filter(shape3DElementConfig -> shape3DElementConfig.getId().equals(elementId))
                .findFirst()
                .flatMap(shape3DElementConfig -> shape3DElementConfig.getVertexContainerMaterialConfigs().stream()
                        .filter(vertexContainerMaterialConfig -> vertexContainerMaterialConfig.getMaterialId().equals(materialId))
                        .findFirst())
                .orElse(null);
    }
}
