package com.btxtech.server.persistence;

import com.btxtech.server.persistence.surface.PhongMaterialConfigEmbeddable;
import com.btxtech.shared.datatypes.shape.VertexContainerMaterial;
import com.btxtech.shared.datatypes.shape.config.VertexContainerMaterialConfig;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import static com.btxtech.server.persistence.surface.PhongMaterialConfigEmbeddable.factorize;

@Entity
@Table(name = "COLLADA_MATERIAL")
public class ColladaMaterialEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String materialId;
    private String materialName;
    private Double alphaToCoverage;
    private boolean characterRepresenting;
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "phongTextureId")),
            @AssociationOverride(name = "normalMap", joinColumns = @JoinColumn(name = "phongNormalMapId")),
            @AssociationOverride(name = "bumpMap", joinColumns = @JoinColumn(name = "phongBumpMapId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "phongScale")),
            @AttributeOverride(name = "normalMapDepth", column = @Column(name = "phongNormalMapDepth")),
            @AttributeOverride(name = "bumpMapDepth", column = @Column(name = "phongBumpMapDepth")),
            @AttributeOverride(name = "shininess", column = @Column(name = "phongShininess")),
            @AttributeOverride(name = "specularStrength", column = @Column(name = "phongSpecularStrength")),
    })
    @Embedded
    private PhongMaterialConfigEmbeddable phongMaterial;
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "phong2TextureId")),
            @AssociationOverride(name = "normalMap", joinColumns = @JoinColumn(name = "phong2NormalMapId")),
            @AssociationOverride(name = "bumpMap", joinColumns = @JoinColumn(name = "phong2BumpMapId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "phong2Scale")),
            @AttributeOverride(name = "normalMapDepth", column = @Column(name = "phong2NormalMapDepth")),
            @AttributeOverride(name = "bumpMapDepth", column = @Column(name = "phong2BumpMapDepth")),
            @AttributeOverride(name = "shininess", column = @Column(name = "phong2Shininess")),
            @AttributeOverride(name = "specularStrength", column = @Column(name = "phong2SpecularStrength")),
    })
    @Embedded
    private PhongMaterialConfigEmbeddable phongMaterial2;

    public String getMaterialId() {
        return materialId;
    }

    public VertexContainerMaterial to() {
        return new VertexContainerMaterial()
                .materialId(materialId)
                .materialName(materialName)
                .alphaToCoverage(alphaToCoverage)
                .characterRepresenting(characterRepresenting)
                .phongMaterialConfig(PersistenceUtil.toConfig(phongMaterial, PhongMaterialConfigEmbeddable::to))
                .phongMaterial2Config(PersistenceUtil.toConfig(phongMaterial2, PhongMaterialConfigEmbeddable::to));
    }

    public ColladaMaterialEntity from(VertexContainerMaterialConfig vertexContainerMaterialConfig, ImagePersistence imagePersistence) {
        materialId = vertexContainerMaterialConfig.getMaterialId();
        materialName = vertexContainerMaterialConfig.getMaterialName();
        alphaToCoverage = vertexContainerMaterialConfig.getAlphaToCoverage();
        characterRepresenting = vertexContainerMaterialConfig.isCharacterRepresenting();
        phongMaterial = factorize(vertexContainerMaterialConfig.getPhongMaterialConfig(), imagePersistence);
        phongMaterial2 = factorize(vertexContainerMaterialConfig.getPhongMaterial2Config(), imagePersistence);
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

        ColladaMaterialEntity that = (ColladaMaterialEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
