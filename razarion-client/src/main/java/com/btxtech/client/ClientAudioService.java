package com.btxtech.client;

import com.btxtech.shared.rest.RestUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.google.gwt.dom.client.MediaElement;
import elemental.client.Browser;
import elemental.html.AudioElement;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Beat
 * 25.12.2016.
 */
@ApplicationScoped
public class ClientAudioService extends AudioService {
    private static final int PARALLEL_PLAY_COUNT = 5;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<Integer, Collection<AudioElement>> audios = new HashMap<>();
    private boolean isMute = false;

    @Override
    protected void playAudio(int audioId) {
        try {
            AudioElement audio = getAudio(audioId);
            if (audio != null) {
                audio.play();
            }
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    public void mute(boolean mute) {
        if (mute == isMute) {
            return;
        }
        isMute = mute;
        for (Collection<AudioElement> audios : audios.values()) {
            for (AudioElement audio : audios) {
                setVolume(audio);
            }
        }
    }

    private AudioElement getAudio(int audioId) {
        try {
            Collection<AudioElement> available = audios.computeIfAbsent(audioId, k -> new ArrayList<>());
            AudioElement audio = null;
            for (Iterator<AudioElement> iterator = available.iterator(); iterator.hasNext(); ) {
                AudioElement availableAudio = iterator.next();
                if (availableAudio.getNetworkState() == MediaElement.NETWORK_NO_SOURCE) {
                    iterator.remove();
                    continue;
                }
                if (availableAudio.isEnded() || availableAudio.isPaused()) {
                    audio = availableAudio;
                    break;
                }
            }
            if (audio != null) {
                audio.setCurrentTime(0);
                return audio;
            }
            if (available.size() < PARALLEL_PLAY_COUNT) {
                audio = Browser.getDocument().createAudioElement();
                setVolume(audio);
                audio.setSrc(RestUrl.getAudioServiceUrl(audioId));
                available.add(audio);
                return audio;
            } else {
                return null;
            }
        } catch (Exception e) {
            exceptionHandler.handleException("ClientAudioService.getAudio() " + audioId, e);
            return null;
        }
    }

    private void setVolume(AudioElement audio) {
        if (isMute) {
            audio.setVolume(0.0f);
        } else {
            audio.setVolume(1.0f);
        }
    }

}
