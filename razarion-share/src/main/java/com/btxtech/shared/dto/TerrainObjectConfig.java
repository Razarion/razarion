package com.btxtech.shared.dto;

import com.btxtech.shared.datatypes.shape.Shape3D;
import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Portable
public class TerrainObjectConfig {
    private int id;
    private Shape3D shape3D;

    public int getId() {
        return id;
    }

    public TerrainObjectConfig setId(int id) {
        this.id = id;
        return this;
    }

    public Shape3D getShape3D() {
        return shape3D;
    }

    public TerrainObjectConfig setShape3D(Shape3D shape3D) {
        this.shape3D = shape3D;
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

        TerrainObjectConfig that = (TerrainObjectConfig) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
