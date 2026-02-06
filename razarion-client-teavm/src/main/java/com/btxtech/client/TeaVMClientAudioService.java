package com.btxtech.client;

import com.btxtech.client.jso.JsConsole;
import com.btxtech.client.jso.JsHTMLAudioElement;
import com.btxtech.shared.CommonUrl;
import com.btxtech.shared.gameengine.InitializeService;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.SelectionEventService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Singleton
public class TeaVMClientAudioService extends AudioService {
    private static final int PARALLEL_PLAY_COUNT = 5;
    private static final double ENVIRONMENT_VOLUME = 0.1;
    private final Map<Integer, Collection<JsHTMLAudioElement>> audios = new HashMap<>();
    private final Map<Integer, JsHTMLAudioElement> terrainLoopAudios = new HashMap<>();
    private boolean isMute = false;

    @Inject
    public TeaVMClientAudioService(AlarmService alarmService,
                                   ItemTypeService itemTypeService,
                                   Provider<TerrainUiService> terrainUiService,
                                   InitializeService initializeService,
                                   SelectionEventService selectionEventService) {
        super(alarmService, itemTypeService, terrainUiService, initializeService, selectionEventService);
    }

    @Override
    protected void playAudio(int audioId) {
        try {
            JsHTMLAudioElement audio = getAudio(audioId);
            if (audio != null) {
                audio.play();
            }
        } catch (Throwable throwable) {
            JsConsole.error("playAudio failed: " + audioId + " " + throwable.getMessage());
        }
    }

    @Override
    protected void playTerrainLoopAudio(int audioId, double volume) {
        try {
            JsHTMLAudioElement audio = terrainLoopAudios.get(audioId);
            if (audio == null) {
                audio = JsHTMLAudioElement.create();
                audio.setSrc(CommonUrl.getAudioServiceUrl(audioId));
                audio.play();
                audio.setLoop(true);
                terrainLoopAudios.put(audioId, audio);
            }
            audio.setVolume(volume * ENVIRONMENT_VOLUME);
        } catch (Throwable throwable) {
            JsConsole.error("playTerrainLoopAudio failed: " + audioId + " " + throwable.getMessage());
        }
    }

    @Override
    public void muteTerrainLoopAudio() {
        for (JsHTMLAudioElement audioElement : terrainLoopAudios.values()) {
            audioElement.setVolume(0);
        }
    }

    private JsHTMLAudioElement getAudio(int audioId) {
        try {
            Collection<JsHTMLAudioElement> available = audios.computeIfAbsent(audioId, k -> new ArrayList<>());
            JsHTMLAudioElement audio = null;
            for (Iterator<JsHTMLAudioElement> iterator = available.iterator(); iterator.hasNext(); ) {
                JsHTMLAudioElement availableAudio = iterator.next();
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
                audio = JsHTMLAudioElement.create();
                setVolume(audio);
                audio.setSrc(CommonUrl.getAudioServiceUrl(audioId));
                available.add(audio);
                return audio;
            } else {
                return null;
            }
        } catch (Exception e) {
            JsConsole.error("TeaVMClientAudioService.getAudio() " + audioId + " " + e.getMessage());
            return null;
        }
    }

    private void setVolume(JsHTMLAudioElement audio) {
        if (isMute) {
            audio.setVolume(0.0);
        } else {
            audio.setVolume(1.0);
        }
    }
}
