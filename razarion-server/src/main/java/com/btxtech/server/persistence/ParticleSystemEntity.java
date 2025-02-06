package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

@Entity
@Table(name = "PARTICLE_SYSTEM")
public class ParticleSystemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity threeJsModelConfig;
    private String emitterNodeId;
    private Vertex positionOffset;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ImageLibraryEntity imageId;


    public Integer getId() {
        return id;
    }

    public ParticleSystemConfig toConfig() {
        return new ParticleSystemConfig()
                .id(id)
                .internalName(internalName)
                .emitterNodeId(emitterNodeId)
                .threeJsModelId(extractId(threeJsModelConfig, ThreeJsModelConfigEntity::getId))
                .positionOffset(positionOffset)
                .imageId(extractId(imageId, ImageLibraryEntity::getId));
    }

    public void fromConfig(ParticleSystemConfig config, ThreeJsModelCrudPersistence threeJsModelCrudPersistence, ImagePersistence imagePersistence) {
        this.internalName = config.getInternalName();
        threeJsModelConfig = threeJsModelCrudPersistence.getEntity(config.getThreeJsModelId());
        emitterNodeId = config.getEmitterNodeId();
        positionOffset = config.getPositionOffset();
        imageId = imagePersistence.getImageLibraryEntity(config.getImageId());
    }
}
