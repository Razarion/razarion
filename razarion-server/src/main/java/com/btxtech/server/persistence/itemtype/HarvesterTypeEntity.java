package com.btxtech.server.persistence.itemtype;

import com.btxtech.server.persistence.ColladaEntity;
import com.btxtech.server.persistence.Shape3DPersistence;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.BuilderType;
import com.btxtech.shared.gameengine.datatypes.itemtype.HarvesterType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Beat
 * 13.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_HARVESTER_TYPE")
public class HarvesterTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private int harvestRange;
    private double progress;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "animationOriginX")),
            @AttributeOverride(name = "y", column = @Column(name = "animationOriginY")),
            @AttributeOverride(name = "z", column = @Column(name = "animationOriginZ")),
    })
    private Vertex animationOrigin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity animationShape3d;

    public HarvesterType toHarvesterType() {
        HarvesterType harvesterType = new HarvesterType().setRange(harvestRange).setProgress(progress);
        if (animationOrigin != null) {
            harvesterType.setAnimationOrigin(animationOrigin);
        }
        if (animationShape3d != null) {
            harvesterType.setAnimationShape3dId(animationShape3d.getId());
        }
        return harvesterType;
    }

    public void fromHarvesterType(HarvesterType harvesterType, Shape3DPersistence shape3DPersistence) {
        harvestRange = harvesterType.getRange();
        progress = harvesterType.getProgress();
        animationOrigin = harvesterType.getAnimationOrigin();
        animationShape3d = shape3DPersistence.getColladaEntity(harvesterType.getAnimationShape3dId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HarvesterTypeEntity that = (HarvesterTypeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
