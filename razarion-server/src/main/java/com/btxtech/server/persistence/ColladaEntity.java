package com.btxtech.server.persistence;

import com.btxtech.servercommon.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.Shape3D;
import com.btxtech.shared.gameengine.datatypes.itemtype.ItemState;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Map;

/**
 * Created by Beat
 * 16.08.2016.
 */
@Entity
@Table(name = "COLLADA")
public class ColladaEntity implements ColladaConverterMapper {
    @Id
    @GeneratedValue
    private Long id;
    @Lob
    @Basic(optional = false)
    private String colladaString;
    @ManyToMany
    @CollectionTable(name = "COLLADA_TEXTURES")
    private Map<String, ImageLibraryEntity> textures;

    public Long getId() {
        return id;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    @Override
    public Integer getTextureId(String materialIdString) {
        ImageLibraryEntity imageLibraryEntity = textures.get(materialIdString);
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId().intValue();
        } else {
            return null;
        }
    }

    @Override
    public ItemState getItemState(String animationId) {
        return null;
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

        ColladaEntity that = (ColladaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
