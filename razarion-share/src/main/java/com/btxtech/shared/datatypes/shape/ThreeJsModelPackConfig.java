package com.btxtech.shared.datatypes.shape;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.Config;
import jsinterop.annotations.JsType;

@JsType
public class ThreeJsModelPackConfig implements Config {
    private int id;
    private String internalName;
    private Integer threeJsModelId;
    private String[] namePath;
    private Vertex position;
    private Vertex scale;
    private Vertex rotation;

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

    public String[] getNamePath() {
        return namePath;
    }

    public void setNamePath(String[] namePath) {
        this.namePath = namePath;
    }

    public Vertex getPosition() {
        return position;
    }

    public void setPosition(Vertex position) {
        this.position = position;
    }

    public Vertex getScale() {
        return scale;
    }

    public void setScale(Vertex scale) {
        this.scale = scale;
    }

    public Vertex getRotation() {
        return rotation;
    }

    public void setRotation(Vertex rotation) {
        this.rotation = rotation;
    }

    public ThreeJsModelPackConfig id(int id) {
        this.id = id;
        return this;
    }

    public ThreeJsModelPackConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public ThreeJsModelPackConfig threeJsModelId(Integer threeJsModelId) {
        setThreeJsModelId(threeJsModelId);
        return this;
    }

    public ThreeJsModelPackConfig namePath(String[] namePath) {
        setNamePath(namePath);
        return this;
    }

    public ThreeJsModelPackConfig position(Vertex position) {
        setPosition(position);
        return this;
    }

    public ThreeJsModelPackConfig scale(Vertex scale) {
        setScale(scale);
        return this;
    }

    public ThreeJsModelPackConfig rotation(Vertex rotation) {
        setRotation(rotation);
        return this;
    }
}
