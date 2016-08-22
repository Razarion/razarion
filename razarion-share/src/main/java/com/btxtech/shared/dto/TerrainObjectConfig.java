package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.shape.Shape3D;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
@Bindable
public class TerrainObjectConfig {
    private int id;
    private String internalName;
    private Integer shape3DId;

    public int getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public TerrainObjectConfig setInternalName(String internalName) {
        this.internalName = internalName;
        return this;
    }

    public TerrainObjectConfig setId(int id) {
        this.id = id;
        return this;
    }

    public Integer getShape3DId() {
        return shape3DId;
    }

    public TerrainObjectConfig setShape3DId(Integer shape3DId) {
        this.shape3DId = shape3DId;
        return this;
    }

    public ObjectNameId createSlopeNameId() {
        return new ObjectNameId(id, internalName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectConfig that = (TerrainObjectConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
