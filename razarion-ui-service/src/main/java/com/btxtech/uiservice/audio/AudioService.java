package com.btxtech.uiservice.audio;

import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * 24.12.2016.
 */
public abstract class AudioService {
    private AudioConfig audioConfig;

    protected abstract void playAudio(int audioId);

    public void initialise(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public void onDialogOpened(Integer audioId) {
        if (audioId != null) {
            playAudio(audioId);
        }
    }

    public void onDialogClosed() {
        if (audioConfig.getDialogClosed() != null) {
            playAudio(audioConfig.getDialogClosed());
        }
    }

    public void onQuestActivated() {
        if (audioConfig.getOnQuestActivated() != null) {
            playAudio(audioConfig.getOnQuestActivated());
        }
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }
}
