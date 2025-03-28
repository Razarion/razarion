package com.btxtech.server.model.engine;

import com.btxtech.server.model.BaseEntity;
import com.btxtech.shared.dto.AudioItemConfig;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "AUDIO_LIBRARY")
public class AudioLibraryEntity extends BaseEntity {
    @Lob
    private byte[] data;
    private String type;

    private long size;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public AudioItemConfig toAudioItemConfig() {
        return new AudioItemConfig()
                .id(getId())
                .internalName(getInternalName())
                .size((int) size)
                .type(type);
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
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : System.identityHashCode(this);
    }
}
