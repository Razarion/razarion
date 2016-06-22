package com.btxtech.server.terrain.object;

import com.btxtech.server.rest.ImageLibraryEntity;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Map;

/**
 * Created by Beat
 * 10.05.2016.
 */
@Entity
@Table(name = "TERRAIN_OBJECT")
public class TerrainObjectEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String internalName;
    @Lob
    @Basic(optional = false)
    private String colladaString;
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    // @MapKey
    @CollectionTable(name = "TERRAIN_OBJECT_TEXTURES")
    private Map<String, ImageLibraryEntity> textures;

    public Long getId() {
        return id;
    }

    public String getInternalName() {
        return internalName;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    public Integer getTextureId(String materialIdString) {
        ImageLibraryEntity imageLibraryEntity = textures.get(materialIdString);
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId().intValue();
        } else {
            return null;
        }
    }

    public void setTextures(Map<String, ImageLibraryEntity> textures) {
        this.textures.clear();
        this.textures.putAll(textures);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TerrainObjectEntity that = (TerrainObjectEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
