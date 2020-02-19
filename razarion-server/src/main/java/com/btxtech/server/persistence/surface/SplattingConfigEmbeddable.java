package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.dto.SplattingConfig;

import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@Embeddable
@MappedSuperclass
public class SplattingConfigEmbeddable {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity image;
    private double scale;
    private double blur;
    private double offset;
    private double amplitude;

    public void from(SplattingConfig splattingConfig, ImagePersistence imagePersistence) {
        image = imagePersistence.getImageLibraryEntity(splattingConfig.getImageId());
        scale = splattingConfig.getScale();
        blur = splattingConfig.getBlur();
        offset = splattingConfig.getOffset();
        amplitude = splattingConfig.getAmplitude();
    }

    public SplattingConfig to() {
        SplattingConfig splattingConfig = new SplattingConfig()
                .scale(scale)
                .blur(blur)
                .offset(offset)
                .amplitude(amplitude);
        if (image != null) {
            splattingConfig.setImageId(image.getId());
        }
        return splattingConfig;
    }

}
