package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.shape.ThreeJsModelConfig;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 02.05.2016.
 */
@Entity
@Table(name = "THREE_JS_MODEL")
public class ThreeJsModelConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    private String fbxGuidHint;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data;
    @Enumerated(EnumType.STRING)
    private ThreeJsModelConfig.Type type;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity nodeMaterial;

    public Integer getId() {
        return id;
    }

    public ThreeJsModelConfig toConfig() {
        return new ThreeJsModelConfig()
                .id(id)
                .internalName(internalName)
                .fbxGuidHint(fbxGuidHint)
                .type(type)
                .nodeMaterialId(extractId(nodeMaterial, ThreeJsModelConfigEntity::getId));
    }

    public void from(ThreeJsModelConfig config, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = config.getInternalName();
        fbxGuidHint = config.getFbxGuidHint();
        type = config.getType();
        nodeMaterial = threeJsModelCrudPersistence.getEntity(config.getNodeMaterialId());
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThreeJsModelConfigEntity that = (ThreeJsModelConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
