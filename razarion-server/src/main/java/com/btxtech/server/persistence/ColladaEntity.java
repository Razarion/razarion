package com.btxtech.server.persistence;

import com.btxtech.server.collada.ColladaConverter;
import com.btxtech.server.collada.ColladaConverterMapper;
import com.btxtech.shared.datatypes.shape.AnimationTrigger;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.datatypes.shape.config.Shape3DConfig;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "colladaEntityId", nullable = false)
    private List<ColladaMaterialEntity> colladaMaterials;
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
            return ColladaConverter.createShape3DBuilder(colladaString, this, null).createShape3DConfig(id)
                    .internalName(internalName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VertexContainerMaterial toVertexContainerMaterial(String materialId) {
        if (colladaMaterials == null) {
            return null;
        }

        return colladaMaterials.stream()
                .filter(colladaMaterialEntity -> colladaMaterialEntity.getMaterialId().equals(materialId))
                .findFirst()
                .map(ColladaMaterialEntity::to)
                .orElse(null);
    }

    public void setColladaMaterials(List<ColladaMaterialEntity> colladaMaterials) {
        if(this.colladaMaterials == null) {
            this.colladaMaterials = new ArrayList<>();
        }
        this.colladaMaterials.clear();
        this.colladaMaterials.addAll(colladaMaterials);
    }

    @Override
    public AnimationTrigger getAnimationTrigger(String animationId) {
        return animations.get(animationId);
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setAnimations(Map<String, AnimationTrigger> animations) {
        if(this.animations == null) {
            this.animations = new HashMap<>();
        }
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
