package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.PhongMaterialConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

@Embeddable
public class PhongMaterialConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double scale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity normalMap;
    private Double normalMapDepth;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity bumpMap;
    private Double bumpMapDepth;
    private Double shininess;
    private Double specularStrength;

    public void from(PhongMaterialConfig phongMaterialConfig, ImagePersistence imagePersistence) {
        texture = imagePersistence.getImageLibraryEntity(phongMaterialConfig.getTextureId());
        scale = phongMaterialConfig.getScale();
        normalMap = imagePersistence.getImageLibraryEntity(phongMaterialConfig.getNormalMapId());
        normalMapDepth = phongMaterialConfig.getNormalMapDepth();
        bumpMap = imagePersistence.getImageLibraryEntity(phongMaterialConfig.getBumpMapId());
        bumpMapDepth = phongMaterialConfig.getBumpMapDepth();
        shininess = phongMaterialConfig.getShininess();
        specularStrength = phongMaterialConfig.getSpecularStrength();
    }

    public PhongMaterialConfig to() {
        return new PhongMaterialConfig()
                .scale(scale)
                .shininess(shininess)
                .specularStrength(specularStrength)
                .textureId(extractId(texture, ImageLibraryEntity::getId))
                .normalMapId(extractId(normalMap, ImageLibraryEntity::getId))
                .normalMapDepth(normalMapDepth)
                .bumpMapId(extractId(bumpMap, ImageLibraryEntity::getId))
                .bumpMapDepth(bumpMapDepth);
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
