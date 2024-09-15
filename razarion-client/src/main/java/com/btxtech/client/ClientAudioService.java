package com.btxtech.client;

import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.system.ExceptionHandler;
import com.btxtech.uiservice.audio.AudioService;
import com.google.gwt.dom.client.MediaElement;
import elemental2.dom.DomGlobal;
import elemental2.dom.HTMLAudioElement;

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
    @Inject
    private ExceptionHandler exceptionHandler;
    private final Map<Integer, Collection<HTMLAudioElement>> audios = new HashMap<>();
    private final Map<Integer, HTMLAudioElement> terrainLoopAudios = new HashMap<>();
    private boolean isMute = false;

    @Override
    protected void playAudio(int audioId) {
        try {
            HTMLAudioElement audio = getAudio(audioId);
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
            HTMLAudioElement audio = terrainLoopAudios.get(audioId);
            if (audio == null) {
                audio = (HTMLAudioElement) DomGlobal.document.createElement("audio");
                audio.src = CommonUrl.getAudioServiceUrl(audioId);
                audio.play();
                audio.loop = true;
                terrainLoopAudios.put(audioId, audio);
            }
            audio.volume = (float) (volume * ENVIRONMENT_VOLUME);
        } catch (Throwable throwable) {
            exceptionHandler.handleException(throwable);
        }
    }

    @Override
    public void muteTerrainLoopAudio() {
        for (HTMLAudioElement audioElement : terrainLoopAudios.values()) {
            audioElement.volume = 0;
        }
    }

    public void mute(boolean mute) {
        if (mute == isMute) {
            return;
        }
        isMute = mute;
        for (Collection<HTMLAudioElement> audios : audios.values()) {
            for (HTMLAudioElement audio : audios) {
                setVolume(audio);
            }
        }
    }

    private HTMLAudioElement getAudio(int audioId) {
        try {
            Collection<HTMLAudioElement> available = audios.computeIfAbsent(audioId, k -> new ArrayList<>());
            HTMLAudioElement audio = null;
            for (Iterator<HTMLAudioElement> iterator = available.iterator(); iterator.hasNext(); ) {
                HTMLAudioElement availableAudio = iterator.next();
                if (availableAudio.networkState == MediaElement.NETWORK_NO_SOURCE) {
                    iterator.remove();
                    continue;
                }
                // TODO ??? if (terrainLoopAudios == availableAudio) {
                // TODO ???    break;
                // TODO ???}
                if (availableAudio.ended || availableAudio.paused) {
                    audio = availableAudio;
                    break;
                }
            }
            if (audio != null) {
                audio.currentTime = 0;
                return audio;
            }
            if (available.size() < PARALLEL_PLAY_COUNT) {
                audio = (HTMLAudioElement) DomGlobal.document.createElement("audio");
                setVolume(audio);
                audio.src = CommonUrl.getAudioServiceUrl(audioId);
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

    private void setVolume(HTMLAudioElement audio) {
        if (isMute) {
            audio.volume = 0.0f;
        } else {
            audio.volume = 1.0f;
        }
    }

}
