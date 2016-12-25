package com.btxtech.client.editor.audio;

import com.btxtech.shared.dto.AudioItemConfig;
import org.jboss.errai.databinding.client.api.Bindable;

/**
 * Created by Beat
 * 24.12.2016.
 */
@Bindable
public class AudioGalleryItem {
    private AudioItemConfig audioItemConfig = new AudioItemConfig();
    private String newInternalName;
    private String dataUrl;

    public AudioGalleryItem init(AudioItemConfig audioItemConfig) {
        this.audioItemConfig = audioItemConfig;
        return this;
    }

    public int getId() {
        return audioItemConfig.getId();
    }

    public String getType() {
        return audioItemConfig.getType();
    }

    public int getSize() {
        return audioItemConfig.getSize();
    }

    public String getInternalName() {
        if (newInternalName != null) {
            return newInternalName;
        } else {
            return audioItemConfig.getInternalName();
        }
    }

    public void setInternalName(String internalName) {
        newInternalName = internalName;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public boolean isDirty() {
        return dataUrl != null || newInternalName != null;
    }

    public AudioItemConfig createAudioConfig() {
        return new AudioItemConfig().setId(audioItemConfig.getId()).setInternalName(newInternalName).setDataUrl(dataUrl);
    }
}
