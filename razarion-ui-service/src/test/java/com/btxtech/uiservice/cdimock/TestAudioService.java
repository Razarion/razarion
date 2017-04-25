package com.btxtech.uiservice.cdimock;

import com.btxtech.uiservice.audio.AudioService;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 24.01.2017.
 */
@ApplicationScoped
public class TestAudioService extends AudioService {
    @Override
    protected void playAudio(int audioId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void playTerrainLoopAudio(int audioId, double volume) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void muteTerrainLoopAudio() {
        throw new UnsupportedOperationException();
    }
}
