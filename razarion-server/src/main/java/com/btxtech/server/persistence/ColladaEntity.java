package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.Shape3DConfig;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @Lob
    private String colladaString;
    @ManyToMany
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @CollectionTable(name = "COLLADA_TEXTURES")
    private Map<String, ImageLibraryEntity> textures;
    @ManyToMany
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @CollectionTable(name = "COLLADA_BUMP_MAPS")
    private Map<String, ImageLibraryEntity> bumpMaps;
    @ElementCollection
    @CollectionTable(name = "COLLADA_BUMP_MAP_DEPTS")
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private Map<String, Double> bumpMapDepts;
    @ElementCollection
    @CollectionTable(name = "COLLADA_ALPHA_TO_COVERAGE")
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private Map<String, Boolean> alphaToCoverages;
    @ElementCollection
    @CollectionTable(name = "COLLADA_CHARACTER_REPRESENTING")
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    private Map<String, Boolean> characterRepresentings;
    @ElementCollection
    @CollectionTable(name = "COLLADA_ANIMATIONS")
    @MapKeyColumn(length = 180) // Only 767 bytes are as key allowed in MariaDB. If character set is utf8mb4 one character uses 4 bytes
    @Enumerated(EnumType.STRING)
    private Map<String, AnimationTrigger> animations;

    public Integer getId() {
        return id;
    }

    public String getColladaString() {
        return colladaString;
    }

    public void setColladaString(String colladaString) {
        this.colladaString = colladaString;
    }

    public Shape3DConfig toShape3DConfig() {
        try {
            return ColladaConverter.createShape3DBuilder(colladaString, this).createShape3DConfig(id)
                    .internalName(internalName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Integer getTextureId(String materialId) {
        ImageLibraryEntity imageLibraryEntity = textures.get(materialId);
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId();
        } else {
            return null;
        }
    }

    @Override
    public Integer getBumpMapId(String materialId) {
        ImageLibraryEntity imageLibraryEntity = bumpMaps.get(materialId);
        if (imageLibraryEntity != null) {
            return imageLibraryEntity.getId();
        } else {
            return null;
        }
    }

    @Override
    public Double getBumpMapDepth(String materialId) {
        return bumpMapDepts.get(materialId);
    }

    @Override
    public boolean getAlphaToCoverage(String materialId) {
        Boolean alphaToCoverage = alphaToCoverages.get(materialId);
        return alphaToCoverage != null && alphaToCoverage;
    }

    @Override
    public boolean isCharacterRepresenting(String materialId) {
        Boolean characterRepresenting = characterRepresentings.get(materialId);
        return characterRepresenting != null && characterRepresenting;
    }

    @Override
    public AnimationTrigger getAnimationTrigger(String animationId) {
        return animations.get(animationId);
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setTextures(Map<String, ImageLibraryEntity> textures) {
        this.textures.clear();
        this.textures.putAll(textures);
    }

    public void setBumpMaps(Map<String, ImageLibraryEntity> bumpMaps) {
        this.bumpMaps.clear();
        this.bumpMaps.putAll(bumpMaps);
    }

    public void setBumpMapDepts(Map<String, Double> bumpMapDepts) {
        this.bumpMapDepts.clear();
        this.bumpMapDepts.putAll(bumpMapDepts);
    }

    public void setAlphaToCoverages(Map<String, Boolean> alphaToCoverages) {
        this.alphaToCoverages.clear();
        this.alphaToCoverages.putAll(alphaToCoverages);
    }

    public void setCharacterRepresentings(Map<String, Boolean> characterRepresentings) {
        this.characterRepresentings.clear();
        if (characterRepresentings != null) {
            this.characterRepresentings.putAll(characterRepresentings);
        }
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
