package com.btxtech.server.persistence;

import com.btxtech.shared.dto.ClipConfig;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by Beat
 * 15.10.2016.
 */
@Entity
@Table(name = "CLIPS")
public class ClipEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String internalName;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ColladaEntity shape3D;
    private Integer soundId;
    private Integer durationMillis;

    public ClipConfig toClipConfig() {
        Integer shape3DId = shape3D != null ? shape3D.getId().intValue() : null;
        return new ClipConfig().setId(id.intValue()).setInternalName(internalName).setSoundId(soundId).setShape3DId(shape3DId).setDurationMillis(durationMillis);
    }

    public void fromClipConfig(ClipConfig clipConfig) {
        internalName = clipConfig.getInternalName();
        soundId = clipConfig.getSoundId();
        durationMillis = clipConfig.getDurationMillis();
    }

    public void setShape3D(ColladaEntity shape3D) {
        this.shape3D = shape3D;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClipEntity that = (ClipEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
