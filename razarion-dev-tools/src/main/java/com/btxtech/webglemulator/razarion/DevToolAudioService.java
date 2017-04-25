package com.btxtech.webglemulator.razarion;

import com.btxtech.uiservice.audio.AudioService;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 25.12.2016.
 */
@ApplicationScoped
public class DevToolAudioService extends AudioService {

    @Override
    protected void playAudio(int audioId) {
        // System.out.println("#### DevToolAudioService.playAudio(): " + audioId);
    }

    @Override
    protected void playTerrainLoopAudio(int audioId, double volume) {
        // System.out.println("#### DevToolAudioService.playTerrainLoopAudio(): " + audioId + " volume: " + volume);
    }

    @Override
    public void muteTerrainLoopAudio() {

    }
}
