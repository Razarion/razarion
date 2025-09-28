package com.btxtech.shared.dto;

import com.btxtech.shared.system.Nullable;
import jsinterop.annotations.JsType;

import java.util.Objects;

/**
 * Created by Beat
 * 07.05.2016.
 */
@JsType
public class GroundConfig implements Config {
    private int id;
    private String internalName;
    private Integer groundBabylonMaterialId;
    private Integer waterBabylonMaterialId;
    private Integer underWaterBabylonMaterialId;
    private Integer botBabylonMaterialId;
    private Integer botWallBabylonMaterialId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public @Nullable Integer getGroundBabylonMaterialId() {
        return groundBabylonMaterialId;
    }

    public void setGroundBabylonMaterialId(@Nullable Integer groundBabylonMaterialId) {
        this.groundBabylonMaterialId = groundBabylonMaterialId;
    }

    public @Nullable Integer getWaterBabylonMaterialId() {
        return waterBabylonMaterialId;
    }

    public void setWaterBabylonMaterialId(@Nullable Integer waterBabylonMaterialId) {
        this.waterBabylonMaterialId = waterBabylonMaterialId;
    }

    public Integer getUnderWaterBabylonMaterialId() {
        return underWaterBabylonMaterialId;
    }

    public void setUnderWaterBabylonMaterialId(Integer underWaterBabylonMaterialId) {
        this.underWaterBabylonMaterialId = underWaterBabylonMaterialId;
    }

    public Integer getBotBabylonMaterialId() {
        return botBabylonMaterialId;
    }

    public void setBotBabylonMaterialId(Integer botBabylonMaterialId) {
        this.botBabylonMaterialId = botBabylonMaterialId;
    }

    public Integer getBotWallBabylonMaterialId() {
        return botWallBabylonMaterialId;
    }

    public void setBotWallBabylonMaterialId(Integer botWallBabylonMaterialId) {
        this.botWallBabylonMaterialId = botWallBabylonMaterialId;
    }

    public GroundConfig id(int id) {
        setId(id);
        return this;
    }

    public GroundConfig internalName(String internalName) {
        setInternalName(internalName);
        return this;
    }

    public GroundConfig groundBabylonMaterialId(Integer groundBabylonMaterialId) {
        setGroundBabylonMaterialId(groundBabylonMaterialId);
        return this;
    }

    public GroundConfig waterBabylonMaterialId(Integer waterBabylonMaterialId) {
        setWaterBabylonMaterialId(waterBabylonMaterialId);
        return this;
    }

    public GroundConfig underWaterBabylonMaterialId(Integer underWaterBabylonMaterialId) {
        setUnderWaterBabylonMaterialId(underWaterBabylonMaterialId);
        return this;
    }

    public GroundConfig botBabylonMaterialId(Integer botBabylonMaterialId) {
        setBotBabylonMaterialId(botBabylonMaterialId);
        return this;
    }

    public GroundConfig botWallBabylonMaterialId(Integer botWallBabylonMaterialId) {
        setBotWallBabylonMaterialId(botWallBabylonMaterialId);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroundConfig that = (GroundConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
