package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
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
    private String colladaString;
    @ManyToMany
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @CollectionTable(name = "COLLADA_TEXTURES")
    private Map<String, ImageLibraryEntity> textures;
    @ElementCollection
    @CollectionTable(name = "COLLADA_ANIMATIONS")
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @Enumerated(EnumType.STRING)
    private Map<String, AnimationTrigger> animations;

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
    public Integer getTextureId(String materialId) {
        ImageLibraryEntity imageLibraryEntity = textures.get(materialId);
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId().intValue();
        } else {
            return null;
        }
    }

    @Override
    public AnimationTrigger getAnimationTrigger(String animationId) {
        return animations.get(animationId);
    }

    public void setTextures(Map<String, ImageLibraryEntity> textures) {
        this.textures.clear();
        this.textures.putAll(textures);
    }

    public void setAnimations(Map<String, AnimationTrigger> animations) {
        this.animations.clear();
        this.animations.putAll(animations);
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
