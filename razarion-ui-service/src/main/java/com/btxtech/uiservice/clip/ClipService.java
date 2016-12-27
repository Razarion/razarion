package com.btxtech.uiservice.clip;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.uiservice.Shape3DUiService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Beat
 * 14.10.2016.
 */
@ApplicationScoped
public class ClipService {
    @Inject
    private Shape3DUiService shape3DUiService;
    @Inject
    private AudioService audioService;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    private Map<Integer, ClipConfig> clips = new HashMap<>();
    private final Collection<PlayingClip> playingClips = new ArrayList<>();

    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        setShapes3Ds(visualConfig.getClipConfigs());
    }

    public void setShapes3Ds(List<ClipConfig> clipConfigs) {
        clips.clear();
        if (clipConfigs != null) {
            for (ClipConfig clipConfig : clipConfigs) {
                clips.put(clipConfig.getId(), clipConfig);
            }
        }
    }

    public Collection<ClipConfig> getClipConfigs() {
        return clips.values();
    }

    public ClipConfig getClipConfig(int clipId) {
        ClipConfig clipConfig = clips.get(clipId);
        if (clipConfig == null) {
            throw new IllegalArgumentException("No ClipConfig for id: " + clipId);
        }
        return clipConfig;
    }

    public void playClip(Vertex position, int clipId, long timeStamp) {
        ClipConfig clipConfig = getClipConfig(clipId);
        playSound(clipConfig, position);
        synchronized (playingClips) {
            playingClips.add(new PlayingClip(position, clipConfig, timeStamp));
        }
    }

    public void playClip(Vertex position, Vertex direction, int clipId, long timeStamp) {
        ClipConfig clipConfig = getClipConfig(clipId);
        playSound(clipConfig, position);
        synchronized (playingClips) {
            playingClips.add(new PlayingClip(position, direction, clipConfig, timeStamp));
        }
    }

    public List<ModelMatrices> provideModelMatrices(ClipConfig clipConfig, long timeStamp) {
        List<ModelMatrices> result = new ArrayList<>();
        synchronized (playingClips) {
            for (Iterator<PlayingClip> iterator = playingClips.iterator(); iterator.hasNext(); ) {
                PlayingClip playingClip = iterator.next();
                if (clipConfig.equals(playingClip.getClipConfig())) {
                    ModelMatrices modelMatrices = playingClip.provideModelMatrices(timeStamp);
                    if (modelMatrices != null) {
                        result.add(modelMatrices);
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return result;
    }

    public void override(ClipConfig clipConfig) {
        clips.put(clipConfig.getId(), clipConfig);
    }

    public void remove(ClipConfig clipConfig) {
        clips.remove(clipConfig.getId());
    }

    private void playSound(ClipConfig clipConfig, Vertex position) {
        if (!terrainScrollHandler.getCurrentViewField().isInside(position.toXY())) {
            return;
        }

        List<Integer> audioIs = clipConfig.getAudioIds();
        if (audioIs != null && !audioIs.isEmpty()) {
            if (audioIs.size() == 1) {
                audioService.onClip(audioIs.get(0));
            } else {
                int index = (int) (audioIs.size() * Math.random());
                audioService.onClip(audioIs.get(index));
            }
        }

    }
}
