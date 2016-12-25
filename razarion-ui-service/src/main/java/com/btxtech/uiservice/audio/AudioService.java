package com.btxtech.uiservice.audio;

import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.rest.RestUrl;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.12.2016.
 */
@ApplicationScoped
public class AudioService {
    private AudioConfig audioConfig;

    public void initialise(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public void onDialogOpened() {
        if (audioConfig.getDialogOpened() != null) {
            playAudio(audioConfig.getDialogOpened());
        }
    }

    public void onDialogClosed() {
        if (audioConfig.getDialogClosed() != null) {
            playAudio(audioConfig.getDialogClosed());
        }
    }

    public void onQuestActivated() {
        if (audioConfig.getOnQuestSActivated() != null) {
            playAudio(audioConfig.getOnQuestSActivated());
        }
    }

    public void onQuestPassed() {
        if (audioConfig.getOnQuestPassed() != null) {
            playAudio(audioConfig.getOnQuestPassed());
        }
    }

    private void playAudio(int audioId) {
        String url = RestUrl.getAudioServiceUrl(audioId);
        // TODO
    }
}
