package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.DoubleSplattingConfig;
import com.btxtech.shared.dto.SplattingConfig;

import javax.persistence.Embeddable;

@Embeddable
public class DoubleSplattingConfigEmbeddable extends SplattingConfigEmbeddable {
    private double scale2;

    public void from(DoubleSplattingConfig doubleSplattingConfig, ImagePersistence imagePersistence) {
        super.from(doubleSplattingConfig, imagePersistence);
        scale2 = doubleSplattingConfig.getScale2();
    }

    @Override
    public DoubleSplattingConfig to() {
        SplattingConfig splattingConfig = super.to();

        DoubleSplattingConfig doubleSplattingConfig = new DoubleSplattingConfig();
        doubleSplattingConfig.scale2(scale2)
                .scale(splattingConfig.getScale())
                .blur(splattingConfig.getBlur())
                .offset(splattingConfig.getOffset())
                .amplitude(splattingConfig.getAmplitude())
                .imageId(splattingConfig.getImageId());
        return doubleSplattingConfig;
    }
}
