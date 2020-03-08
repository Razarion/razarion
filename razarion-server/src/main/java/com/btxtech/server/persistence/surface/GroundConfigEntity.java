package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.GroundConfig;

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

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "topTextureId")),
            @AssociationOverride(name = "bumpMap", joinColumns = @JoinColumn(name = "topBumpMapId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "topScale")),
            @AttributeOverride(name = "bumpMapDepth", column = @Column(name = "topBumpMapDepth")),
            @AttributeOverride(name = "shininess", column = @Column(name = "topShininess")),
            @AttributeOverride(name = "specularStrength", column = @Column(name = "topSpecularStrength")),
    })
    @Embedded
    private PhongMaterialConfigEmbeddable topMaterial;
    @AssociationOverrides({
            @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "bottomTextureId")),
            @AssociationOverride(name = "bumpMap", joinColumns = @JoinColumn(name = "bottomBumpMapId"))
    })
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "bottomScale")),
            @AttributeOverride(name = "bumpMapDepth", column = @Column(name = "bottomBumpMapDepth")),
            @AttributeOverride(name = "shininess", column = @Column(name = "bottomShininess")),
            @AttributeOverride(name = "specularStrength", column = @Column(name = "bottomSpecularStrength")),
    })
    @Embedded
    private PhongMaterialConfigEmbeddable bottomMaterial;
    @AssociationOverride(name = "image", joinColumns = @JoinColumn(name = "splattingImageId"))
    @AttributeOverrides({
            @AttributeOverride(name = "scale", column = @Column(name = "splattingScale")),
            @AttributeOverride(name = "scale2", column = @Column(name = "splattingScale2")),
            @AttributeOverride(name = "blur", column = @Column(name = "splattingBlur")),
            @AttributeOverride(name = "offset", column = @Column(name = "splattingOffset")),
            @AttributeOverride(name = "amplitude", column = @Column(name = "splattingAmplitude")),
    })
    @Embedded
    private DoubleSplattingConfigEmbeddable splatting;

    public Integer getId() {
        return id;
    }

    public GroundConfig toConfig() {
        GroundConfig groundConfig = new GroundConfig();
        groundConfig.id(id).internalName(internalName);
        if (topMaterial != null) {
            groundConfig.setTopMaterial(topMaterial.to());
        }
        if (bottomMaterial != null) {
            groundConfig.setBottomMaterial(bottomMaterial.to());
        }
        if (splatting != null) {
            groundConfig.setSplatting(splatting.to());
        }
        return groundConfig;
    }

    public void fromGroundConfig(GroundConfig config, ImagePersistence imagePersistence) {
        internalName = config.getInternalName();
        if (config.getTopMaterial() != null) {
            topMaterial = new PhongMaterialConfigEmbeddable();
            topMaterial.from(config.getTopMaterial(), imagePersistence);
        } else {
            topMaterial = null;
        }
        if (config.getBottomMaterial() != null) {
            bottomMaterial = new PhongMaterialConfigEmbeddable();
            bottomMaterial.from(config.getBottomMaterial(), imagePersistence);
        } else {
            bottomMaterial = null;
        }
        if (config.getSplatting() != null) {
            splatting = new DoubleSplattingConfigEmbeddable();
            splatting.from(config.getSplatting(), imagePersistence);
        } else {
            splatting = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundConfigEntity that = (GroundConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
