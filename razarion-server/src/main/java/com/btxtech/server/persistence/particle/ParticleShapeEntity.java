package com.btxtech.server.persistence.particle;

import com.btxtech.server.persistence.ImageLibraryEntity;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.shared.datatypes.particle.ParticleShapeConfig;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.util.List;

import static com.btxtech.server.persistence.PersistenceUtil.extractList;
import static com.btxtech.server.persistence.PersistenceUtil.toList;

@Entity
@Table(name = "PARTICLE_SHAPE")
public class ParticleShapeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private double edgeLength;
    private double shadowAlphaCutOff;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity colorRampImage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity alphaOffsetImage;
    @ElementCollection
    @CollectionTable(name = "PARTICLE_SHAPE_COLOR_RAMP_OFFSET", joinColumns = @JoinColumn(name = "particleShape"))
    @OrderColumn(name = "orderColumn")
    private List<Double> colorRampXOffsets;
    private double textureOffsetScope;

    public Integer getId() {
        return id;
    }

    public ParticleShapeConfig toConfig() {
        return new ParticleShapeConfig()
                .id(id)
                .internalName(internalName)
                .edgeLength(edgeLength)
                .shadowAlphaCutOff(shadowAlphaCutOff)
                .colorRampImageId(ImagePersistence.idOrNull(colorRampImage))
                .alphaOffsetImageId(ImagePersistence.idOrNull(alphaOffsetImage))
                .colorRampXOffsets(extractList(colorRampXOffsets))
                .textureOffsetScope(textureOffsetScope);
    }

    public void fromConfig(ParticleShapeConfig config, ImagePersistence imagePersistence) {
        internalName = config.getInternalName();
        edgeLength = config.getEdgeLength();
        shadowAlphaCutOff = config.getShadowAlphaCutOff();
        colorRampImage = imagePersistence.getImageLibraryEntity(config.getColorRampImageId());
        alphaOffsetImage = imagePersistence.getImageLibraryEntity(config.getAlphaOffsetImageId());
        colorRampXOffsets = toList(colorRampXOffsets, config.getColorRampXOffsets());
        textureOffsetScope = config.getTextureOffsetScope();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParticleShapeEntity that = (ParticleShapeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }

}
