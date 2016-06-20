package com.btxtech.server.terrain.object;

import com.btxtech.server.rest.ImageLibraryEntity;
import com.btxtech.shared.dto.TerrainObject;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 19.06.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT_MATERIAL")
public class TerrainObjectMaterialEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    @Enumerated(EnumType.STRING)
    private TerrainObject.Type type;
    @ManyToOne
    private ImageLibraryEntity imageLibraryEntity;

    public String getName() {
        return name;
    }

    public TerrainObject.Type getType() {
        return type;
    }

    public ImageLibraryEntity getImageLibraryEntity() {
        return imageLibraryEntity;
    }

    public void setImageLibraryEntity(ImageLibraryEntity imageLibraryEntity) {
        this.imageLibraryEntity = imageLibraryEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectMaterialEntity that = (TerrainObjectMaterialEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
