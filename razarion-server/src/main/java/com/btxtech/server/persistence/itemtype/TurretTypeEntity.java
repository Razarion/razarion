package com.btxtech.server.persistence.itemtype;

import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.gameengine.datatypes.itemtype.TurretType;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * 19.05.2017.
 */
@Entity
@Table(name = "BASE_ITEM_TURRET_TYPE")
public class TurretTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
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
        return new TurretType().setAngleVelocity(angleVelocity).setTorrentCenter(torrentCenter).setMuzzlePosition(muzzlePosition).setShape3dMaterialId(shape3dMaterialId);
    }

    public void fromTurretType(TurretType turretType) {
        angleVelocity = turretType.getAngleVelocity();
        torrentCenter = turretType.getTorrentCenter();
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
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
