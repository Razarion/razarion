package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.PhongMaterialConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class PhongMaterialConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double scale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bumpMap;
    private Double bumpMapDepth;
    private Double shininess;
    private Double specularStrength;

    public void from(PhongMaterialConfig phongMaterialConfig, ImagePersistence imagePersistence) {
        texture = imagePersistence.getImageLibraryEntity(phongMaterialConfig.getTextureId());
        scale = phongMaterialConfig.getScale();
        bumpMap = imagePersistence.getImageLibraryEntity(phongMaterialConfig.getBumpMapId());
        bumpMapDepth = phongMaterialConfig.getBumpMapDepth();
        shininess = phongMaterialConfig.getShininess();
        specularStrength = phongMaterialConfig.getSpecularStrength();

    }

    public PhongMaterialConfig to() {
        PhongMaterialConfig phongMaterialConfig = new PhongMaterialConfig()
                .scale(scale)
                .bumpMapDepth(bumpMapDepth)
                .shininess(shininess)
                .specularStrength(specularStrength);
        if (texture != null) {
            phongMaterialConfig.setTextureId(texture.getId());
        }
        if (bumpMap != null) {
            phongMaterialConfig.setBumpMapId(bumpMap.getId());
        }
        return phongMaterialConfig;
    }

    public static PhongMaterialConfigEmbeddable factorize(PhongMaterialConfig materialConfig, ImagePersistence imagePersistence) {
        if (materialConfig != null) {
            PhongMaterialConfigEmbeddable materialEnity = new PhongMaterialConfigEmbeddable();
            materialEnity.from(materialConfig, imagePersistence);
            return materialEnity;
        } else {
            return null;
        }
    }

}
