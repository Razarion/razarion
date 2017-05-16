package com.btxtech.server.persistence;

import com.btxtech.shared.dto.AudioItemConfig;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Created by Beat
 * 18.06.2016.
 */
@Entity
@Table(name = "AUDIO_LIBRARY")
public class AudioLibraryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Lob
    private byte[] data;
    private String type;
    private String internalName;
    private long size;

    public Integer getId() {
        return id;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public void setSize(long size) {
        this.size = size;
    }

    AudioItemConfig toAudioConfig() {
        return new AudioItemConfig().setId(id).setInternalName(internalName).setSize((int) size).setType(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AudioLibraryEntity that = (AudioLibraryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }
}
