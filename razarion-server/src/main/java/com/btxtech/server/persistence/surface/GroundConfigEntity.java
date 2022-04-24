package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.shared.dto.GroundConfig;

import javax.persistence.AssociationOverride;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "GROUND_CONFIG")
public class GroundConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity topMaterial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity bottomMaterial;
    @AssociationOverride(name = "texture", joinColumns = @JoinColumn(name = "splattingTextureId"))
    @AttributeOverrides({
            @AttributeOverride(name = "scale1", column = @Column(name = "splattingScale1")),
            @AttributeOverride(name = "scale2", column = @Column(name = "splattingScale2")),
            @AttributeOverride(name = "blur", column = @Column(name = "splattingBlur")),
            @AttributeOverride(name = "offset", column = @Column(name = "splattingOffset")),
    })
    @Embedded
    private GroundSplattingConfigEmbeddable splatting;

    public Integer getId() {
        return id;
    }

    public GroundConfig toConfig() {
        return new GroundConfig()
                .id(id)
                .internalName(internalName)
                .topThreeJsMaterial(PersistenceUtil.extractId(topMaterial, ThreeJsModelConfigEntity::getId))
                .bottomThreeJsMaterial(PersistenceUtil.extractId(bottomMaterial, ThreeJsModelConfigEntity::getId))
                .splatting(PersistenceUtil.toConfig(splatting, GroundSplattingConfigEmbeddable::to));
    }

    public void fromGroundConfig(GroundConfig config, ImagePersistence imagePersistence, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = config.getInternalName();
        topMaterial = threeJsModelCrudPersistence.getEntity(config.getTopThreeJsMaterial());
        bottomMaterial = threeJsModelCrudPersistence.getEntity(config.getBottomThreeJsMaterial());
        if (config.getSplatting() != null) {
            splatting = new GroundSplattingConfigEmbeddable();
            splatting.from(config.getSplatting(), imagePersistence);
        } else {
            splatting = null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroundConfigEntity that = (GroundConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
