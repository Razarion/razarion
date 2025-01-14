package com.btxtech.server.persistence.surface;

import com.btxtech.server.persistence.BabylonMaterialCrudPersistence;
import com.btxtech.server.persistence.ImagePersistence;
import com.btxtech.server.persistence.PersistenceUtil;
import com.btxtech.server.persistence.ThreeJsModelConfigEntity;
import com.btxtech.server.persistence.ThreeJsModelCrudPersistence;
import com.btxtech.server.persistence.ui.BabylonMaterialEntity;
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
    private BabylonMaterialEntity groundBabylonMaterial;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BabylonMaterialEntity waterBabylonMaterial;


    public Integer getId() {
        return id;
    }

    public GroundConfig toConfig() {
        return new GroundConfig()
                .id(id)
                .internalName(internalName)
                .groundBabylonMaterialId(PersistenceUtil.extractId(groundBabylonMaterial, BabylonMaterialEntity::getId))
                .waterBabylonMaterialId(PersistenceUtil.extractId(waterBabylonMaterial, BabylonMaterialEntity::getId));
    }

    public void fromGroundConfig(GroundConfig config, BabylonMaterialCrudPersistence babylonMaterialCrudPersistence) {
        internalName = config.getInternalName();
        groundBabylonMaterial = babylonMaterialCrudPersistence.getEntity(config.getGroundBabylonMaterialId());
        waterBabylonMaterial = babylonMaterialCrudPersistence.getEntity(config.getWaterBabylonMaterialId());
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
