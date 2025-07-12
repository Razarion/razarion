package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.server.model.ui.ParticleSystemEntity;
import com.btxtech.server.service.engine.BaseItemTypeService;
import com.btxtech.server.service.ui.ParticleSystemService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import static com.btxtech.server.service.PersistenceUtil.extractId;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_BUILDER_TYPE")
public class BuilderTypeEntity extends BaseEntity {

    private double buildRange;
    private double progress;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD",
            joinColumns = @JoinColumn(name = "builder"),
            inverseJoinColumns = @JoinColumn(name = "baseItemType"))
    private List<BaseItemTypeEntity> ableToBuilds;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ParticleSystemEntity particleSystem;

    public BuilderType toBuilderType() {
        BuilderType builderType = new BuilderType()
                .range(buildRange)
                .progress(progress)
                .particleSystemConfigId(extractId(particleSystem, ParticleSystemEntity::getId));

        if (ableToBuilds != null && !ableToBuilds.isEmpty()) {
            List<Integer> ableToBuildIds = new ArrayList<>();
            for (BaseItemTypeEntity ableToBuild : ableToBuilds) {
                ableToBuildIds.add(ableToBuild.getId());
            }
            builderType.ableToBuildIds(ableToBuildIds);
        }
        return builderType;
    }

    public void fromBuilderType(BuilderType builderType, BaseItemTypeService baseItemTypeCrudPersistence, ParticleSystemService particleSystemCrudPersistence) {
        buildRange = builderType.getRange();
        progress = builderType.getProgress();
        if (builderType.getAbleToBuildIds() != null && !builderType.getAbleToBuildIds().isEmpty()) {
            if (ableToBuilds == null) {
                ableToBuilds = new ArrayList<>();
            }
            ableToBuilds.clear();
            for (Integer ableToBuildId : builderType.getAbleToBuildIds()) {
                ableToBuilds.add(baseItemTypeCrudPersistence.getEntity(ableToBuildId));
            }
        } else {
            ableToBuilds = null;
        }
        particleSystem = particleSystemCrudPersistence.getEntity(builderType.getParticleSystemConfigId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BuilderTypeEntity that = (BuilderTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
