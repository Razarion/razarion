package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.GroundSplattingConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class GroundSplattingConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity texture;
    private double scale1;
    private double scale2;
    private double blur;
    private double offset;

    public void from(GroundSplattingConfig splattingConfig, ImagePersistence imagePersistence) {
        texture = imagePersistence.getImageLibraryEntity(splattingConfig.getTextureId());
        scale1 = splattingConfig.getScale1();
        scale2 = splattingConfig.getScale2();
        blur = splattingConfig.getBlur();
        offset = splattingConfig.getOffset();
    }

    public GroundSplattingConfig to() {
        GroundSplattingConfig splattingConfig = new GroundSplattingConfig()
                .scale1(scale1)
                .scale2(scale2)
                .blur(blur)
                .offset(offset);
        if (texture != null) {
            splattingConfig.setTextureId(texture.getId());
        }
        return splattingConfig;
    }

}
