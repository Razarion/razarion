package com.btxtech.server.persistence;

import com.btxtech.shared.dto.ClipConfig;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

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
    @ManyToMany
    @JoinTable(name = "CLIPS_AUDIOS")
    private List<AudioLibraryEntity> audios;

    private Integer durationMillis;

    public ClipConfig toClipConfig() {
        Integer shape3DId = shape3D != null ? shape3D.getId().intValue() : null;
        List<Integer> audioIds = new ArrayList<>();
        for (AudioLibraryEntity audio : audios) {
            audioIds.add(audio.getId().intValue());
        }

        return new ClipConfig().setId(id.intValue()).setInternalName(internalName).setAudioIds(audioIds).setShape3DId(shape3DId).setDurationMillis(durationMillis);
    }

    public void fromClipConfig(ClipConfig clipConfig, List<AudioLibraryEntity> audios) {
        internalName = clipConfig.getInternalName();
        this.audios = audios;
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
