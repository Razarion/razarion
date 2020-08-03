package com.btxtech.server.persistence.surface;

import com.btxtech.shared.dto.DrivewayConfig;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by Beat
 * on 08.07.2017.
 */
@Entity
@Table(name = "SLOPE_DRIVEWAY")
public class DrivewayConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String internalName;
    double angle;

    public Integer getId() {
        return id;
    }

    public DrivewayConfig toDrivewayConfig() {
        return new DrivewayConfig().id(id).angle(angle).internalName(internalName);
    }

    public void fromDrivewayConfig(DrivewayConfig config) {
        internalName = config.getInternalName();
        angle = config.getAngle();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DrivewayConfigEntity that = (DrivewayConfigEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
