package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.dto.Config;
import com.btxtech.shared.dto.editor.CollectionReference;
import com.btxtech.shared.dto.editor.CollectionReferenceType;
import jsinterop.annotations.JsType;

@JsType
public class ParticleSystemConfig implements Config {
    private int id;
    private String internalName;
    @CollectionReference(CollectionReferenceType.THREE_JS_MODEL)
    private Integer threeJsModelId;
    private String[] emitterMeshPath;

    @Override
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

    public Integer getThreeJsModelId() {
        return threeJsModelId;
    }

    public void setThreeJsModelId(Integer threeJsModelId) {
        this.threeJsModelId = threeJsModelId;
    }

    public String[] getEmitterMeshPath() {
        return emitterMeshPath;
    }

    public void setEmitterMeshPath(String[] emitterMeshPath) {
        this.emitterMeshPath = emitterMeshPath;
    }


    public ParticleSystemConfig id(int id) {
        this.id = id;
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
}
