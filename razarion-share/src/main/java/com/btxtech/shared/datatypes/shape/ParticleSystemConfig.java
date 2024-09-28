package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

@JsType
public class ParticleSystemConfig implements Config {
    private int id;
    private String internalName;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer threeJsModelId;
    private String[] emitterMeshPath;
    private Vertex positionOffset;
    @CollectionReference(CollectionReferenceType.IMAGE)
    private Integer imageId;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getInternalName() {
        return internalName;
    }

    @Override
    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable Integer getThreeJsModelId() {
        return threeJsModelId;
    }

    public void setThreeJsModelId(@Nullable Integer threeJsModelId) {
        this.threeJsModelId = threeJsModelId;
    }

    public String[] getEmitterMeshPath() {
        return emitterMeshPath;
    }

    public void setEmitterMeshPath(String[] emitterMeshPath) {
        this.emitterMeshPath = emitterMeshPath;
    }

    public @Nullable Vertex getPositionOffset() {
        return positionOffset;
    }

    public void setPositionOffset(@Nullable Vertex positionOffset) {
        this.positionOffset = positionOffset;
    }

    public @Nullable Integer getImageId() {
        return imageId;
    }

    public void setImageId(@Nullable Integer imageId) {
        this.imageId = imageId;
    }

    public ParticleSystemConfig id(int id) {
        setId(id);
        return this;
    }

    public ParticleSystemConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ParticleSystemConfig threeJsModelId(Integer threeJsModelId) {
        setThreeJsModelId(threeJsModelId);
        return this;
    }

    public ParticleSystemConfig emitterMeshPath(String[] emitterMeshPath) {
        setEmitterMeshPath(emitterMeshPath);
        return this;
    }

    public ParticleSystemConfig positionOffset(Vertex positionOffset) {
        setPositionOffset(positionOffset);
        return this;
    }

    public ParticleSystemConfig imageId(Integer imageId) {
        setImageId(imageId);
        return this;
    }
}
