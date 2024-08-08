package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ParticleSystemConfig;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    @ElementCollection
    @CollectionTable(name = "PARTICLE_SYSTEM_EMITTER_MESH_PATH", joinColumns = @JoinColumn(name = "particleSystem"))
    @OrderColumn(name = "orderColumn")
    private List<String> emitterMeshPath;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "positionOffsetX")),
            @AttributeOverride(name = "y", column = @Column(name = "positionOffsetY")),
            @AttributeOverride(name = "z", column = @Column(name = "positionOffsetZ")),
    })
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
                .emitterMeshPath(emitterMeshPath != null ? emitterMeshPath.toArray(new String[0]) : new String[]{})
                .threeJsModelId(extractId(threeJsModelConfig, ThreeJsModelConfigEntity::getId))
                .positionOffset(positionOffset)
                .imageId(extractId(imageId, ImageLibraryEntity::getId));
    }

    public void fromConfig(ParticleSystemConfig config, ThreeJsModelCrudPersistence threeJsModelCrudPersistence, ImagePersistence imagePersistence) {
        this.internalName = config.getInternalName();
        threeJsModelConfig = threeJsModelCrudPersistence.getEntity(config.getThreeJsModelId());
        if (this.emitterMeshPath == null) {
            this.emitterMeshPath = new ArrayList<>();
        }
        this.emitterMeshPath.clear();
        if (config.getEmitterMeshPath() != null) {
            this.emitterMeshPath.addAll(Arrays.asList(config.getEmitterMeshPath()));
        }
        positionOffset = config.getPositionOffset();
        imageId = imagePersistence.getImageLibraryEntity(config.getImageId());
    }
}
