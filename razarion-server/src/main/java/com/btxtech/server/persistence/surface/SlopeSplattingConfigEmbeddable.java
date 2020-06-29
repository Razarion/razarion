package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.gameengine.datatypes.config.SlopeSplattingConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class SlopeSplattingConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double scale;
    private double impact;
    private double blur;
    private double offset;

    public void from(SlopeSplattingConfig splattingConfig, ImagePersistence imagePersistence) {
        texture = imagePersistence.getImageLibraryEntity(splattingConfig.getTextureId());
        scale = splattingConfig.getScale();
        impact = splattingConfig.getImpact();
        blur = splattingConfig.getBlur();
        offset = splattingConfig.getOffset();
    }

    public SlopeSplattingConfig to() {
        SlopeSplattingConfig splattingConfig = new SlopeSplattingConfig()
                .scale(scale)
                .impact(impact)
                .blur(blur)
                .offset(offset);
        if (texture != null) {
            splattingConfig.setTextureId(texture.getId());
        }
        return splattingConfig;
    }

}
