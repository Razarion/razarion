package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;
import jakarta.persistence.*;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_TURRET_TYPE")
public class TurretTypeEntity extends BaseEntity {

    private double angleVelocity;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "torrentCenterX")),
            @AttributeOverride(name = "y", column = @Column(name = "torrentCenterY")),
            @AttributeOverride(name = "z", column = @Column(name = "torrentCenterZ")),
    })
    private Vertex torrentCenter;
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "muzzlePositionX")),
            @AttributeOverride(name = "y", column = @Column(name = "muzzlePositionY")),
            @AttributeOverride(name = "z", column = @Column(name = "muzzlePositionZ")),
    })
    private Vertex muzzlePosition;
    private String shape3dMaterialId;

    public TurretType toTurretType() {
        return new TurretType().setAngleVelocity(angleVelocity).setTurretCenter(torrentCenter).setMuzzlePosition(muzzlePosition).setShape3dMaterialId(shape3dMaterialId);
    }

    public void fromTurretType(TurretType turretType) {
        angleVelocity = turretType.getAngleVelocity();
        torrentCenter = turretType.getTurretCenter();
        muzzlePosition = turretType.getMuzzlePosition();
        shape3dMaterialId = turretType.getShape3dMaterialId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TurretTypeEntity that = (TurretTypeEntity) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
