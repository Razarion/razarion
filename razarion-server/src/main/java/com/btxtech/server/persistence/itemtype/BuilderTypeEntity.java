package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_BUILDER_TYPE")
public class BuilderTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private double buildRange;
    private double progress;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "BASE_ITEM_BUILDER_TYPE_ABLE_TO_BUILD",
            joinColumns = @JoinColumn(name = "builder"),
            inverseJoinColumns = @JoinColumn(name = "baseItemType"))
    private List<BaseItemTypeEntity> ableToBuilds;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "animationOriginX")),
            @AttributeOverride(name = "y", column = @Column(name = "animationOriginY")),
            @AttributeOverride(name = "z", column = @Column(name = "animationOriginZ")),
    })
    private Vertex animationOrigin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity animationShape3d;

    public BuilderType toBuilderType() {
        BuilderType builderType = new BuilderType().setRange(buildRange).setProgress(progress);
        if (ableToBuilds != null && !ableToBuilds.isEmpty()) {
            List<Integer> ableToBuildIds = new ArrayList<>();
            for (BaseItemTypeEntity ableToBuild : ableToBuilds) {
                ableToBuildIds.add(ableToBuild.getId());
            }
            builderType.setAbleToBuildIds(ableToBuildIds);
        }
        if (animationOrigin != null) {
            builderType.setAnimationOrigin(animationOrigin);
        }
        if (animationShape3d != null) {
            builderType.setAnimationShape3dId(animationShape3d.getId());
        }
        return builderType;
    }

    public void fromBuilderType(BuilderType builderType, ItemTypePersistence itemTypePersistence, Shape3DPersistence shape3DPersistence) {
        buildRange = builderType.getRange();
        progress = builderType.getProgress();
        if (builderType.getAbleToBuildIds() != null && !builderType.getAbleToBuildIds().isEmpty()) {
            if (ableToBuilds == null) {
                ableToBuilds = new ArrayList<>();
            }
            ableToBuilds.clear();
            for (int ableToBuildId : builderType.getAbleToBuildIds()) {
                ableToBuilds.add(itemTypePersistence.readBaseItemTypeEntity(ableToBuildId));
            }
        } else {
            ableToBuilds = null;
        }
        animationOrigin = builderType.getAnimationOrigin();
        animationShape3d = shape3DPersistence.getColladaEntity(builderType.getAnimationShape3dId());
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
