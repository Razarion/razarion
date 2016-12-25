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
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private AudioConfig audioConfig;

    protected abstract void playAudio(String audioServiceUrl);

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
        try {
            playAudio(RestUrl.getAudioServiceUrl(audioId));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }
}
