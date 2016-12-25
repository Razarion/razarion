package com.btxtech.client;

import com.btxtech.uiservice.audio.AudioService;
import elemental.client.Browser;
import elemental.html.AudioElement;

import javax.enterprise.context.ApplicationScoped;

/**
 * Created by Beat
 * 25.12.2016.
 */
@ApplicationScoped
public class ClientAudioService extends AudioService {
    @Override
    protected void playAudio(String audioServiceUrl) {
        AudioElement audioElement = Browser.getDocument().createAudioElement();
        audioElement.setSrc(audioServiceUrl);
        audioElement.play();
    }
}
