package com.btxtech.server.persistence;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.datatypes.shape.ThreeJsModelPackConfig;

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

import static com.btxtech.server.persistence.PersistenceUtil.defaultOnNull;
import static com.btxtech.server.persistence.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 06.07.2016.
 */
@Entity
@Table(name = "THREE_JS_MODEL_PACK")
public class ThreeJsModelPackConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ThreeJsModelConfigEntity threeJsModelConfig;
    @ElementCollection
    @CollectionTable(name = "THREE_JS_MODEL_PACK_PATH", joinColumns = @JoinColumn(name = "threeJsModelPackConfig"))
    @OrderColumn(name = "orderColumn")
    private String[] namePath;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "positionX")),
            @AttributeOverride(name = "y", column = @Column(name = "positionY")),
            @AttributeOverride(name = "z", column = @Column(name = "positionZ")),
    })
    private Vertex position;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "scaleX")),
            @AttributeOverride(name = "y", column = @Column(name = "scaleY")),
            @AttributeOverride(name = "z", column = @Column(name = "scaleZ")),
    })
    private Vertex scale;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "rotationX")),
            @AttributeOverride(name = "y", column = @Column(name = "rotationY")),
            @AttributeOverride(name = "z", column = @Column(name = "rotationZ")),
    })
    private Vertex rotation;

    public ThreeJsModelPackConfig toConfig() {
        return new ThreeJsModelPackConfig()
                .id(id)
                .internalName(internalName)
                .namePath(defaultOnNull(namePath, new String[0]))
                .threeJsModelId(extractId(threeJsModelConfig, ThreeJsModelConfigEntity::getId))
                .position(position)
                .rotation(rotation)
                .scale(scale);
    }

    public void from(ThreeJsModelPackConfig config, ThreeJsModelCrudPersistence threeJsModelCrudPersistence) {
        internalName = config.getInternalName();
        threeJsModelConfig = threeJsModelCrudPersistence.getEntity(config.getThreeJsModelId());
        namePath = config.getNamePath();
        position = config.getPosition();
        scale = config.getScale();
        rotation = config.getRotation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThreeJsModelPackConfigEntity that = (ThreeJsModelPackConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
