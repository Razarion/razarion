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
    private static final double ENVIRONMENT_VOLUME = 0.1;
    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    private ExceptionHandler exceptionHandler;
    private Map<Integer, Collection<AudioElement>> audios = new HashMap<>();
    private Map<Integer, AudioElement> terrainLoopAudios = new HashMap<>();
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

    @Override
    protected void playTerrainLoopAudio(int audioId, double volume) {
        try {
            AudioElement audio = terrainLoopAudios.get(audioId);
            if (audio == null) {
                audio = Browser.getDocument().createAudioElement();
                audio.setSrc(RestUrl.getAudioServiceUrl(audioId));
                audio.play();
                audio.setLoop(true);
                terrainLoopAudios.put(audioId, audio);
            }
            audio.setVolume((float) (volume * ENVIRONMENT_VOLUME));
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void muteTerrainLoopAudio() {
        for (AudioElement audioElement : terrainLoopAudios.values()) {
            audioElement.setVolume(0);
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
                if (terrainLoopAudios == availableAudio) {
                    break;
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
