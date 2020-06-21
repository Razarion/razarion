package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.ShallowWaterConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class ShallowWaterConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double scale;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity distortion;
    private double distortionStrength;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity stencil;
    private double durationSeconds;

    public void from(ShallowWaterConfig shallowWaterConfig, ImagePersistence imagePersistence) {
        texture = imagePersistence.getImageLibraryEntity(shallowWaterConfig.getTextureId());
        scale = shallowWaterConfig.getScale();
        distortion = imagePersistence.getImageLibraryEntity(shallowWaterConfig.getDistortionId());
        distortionStrength = shallowWaterConfig.getDistortionStrength();
        stencil = imagePersistence.getImageLibraryEntity(shallowWaterConfig.getStencilId());
        durationSeconds = shallowWaterConfig.getDurationSeconds();
    }

    public ShallowWaterConfig to() {
        return new ShallowWaterConfig()
                .textureId(ImagePersistence.idOrNull(texture))
                .scale(scale)
                .distortionId(ImagePersistence.idOrNull(distortion))
                .distortionStrength(distortionStrength)
                .stencilId(ImagePersistence.idOrNull(stencil))
                .durationSeconds(durationSeconds);
    }

    public static ShallowWaterConfigEmbeddable factorize(ShallowWaterConfig shallowWaterConfig, ImagePersistence imagePersistence) {
        if (shallowWaterConfig != null) {
            ShallowWaterConfigEmbeddable shallowWaterConfigEmbeddable = new ShallowWaterConfigEmbeddable();
            shallowWaterConfigEmbeddable.from(shallowWaterConfig, imagePersistence);
            return shallowWaterConfigEmbeddable;
        } else {
            return null;
        }
    }

    public static ShallowWaterConfig to(ShallowWaterConfigEmbeddable shallowWaterConfigEmbeddable) {
        if (shallowWaterConfigEmbeddable != null) {
            return shallowWaterConfigEmbeddable.to();
        } else {
            return null;
        }

    }
}
