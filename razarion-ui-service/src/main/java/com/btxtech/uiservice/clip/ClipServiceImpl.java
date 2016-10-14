package com.btxtech.uiservice.clip;

import com.btxtech.shared.datatypes.ModelMatrices;
import com.btxtech.shared.datatypes.Vertex;
import com.btxtech.shared.dto.ClipConfig;
import com.btxtech.shared.dto.VisualConfig;
import com.btxtech.shared.gameengine.planet.ClipService;
import com.btxtech.uiservice.Shape3DUiService;

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
public class ClipServiceImpl implements ClipService {
    @Inject
    private Shape3DUiService shape3DUiService;
    private Map<Integer, ClipConfig> clips = new HashMap<>();
    private final Collection<PlayingClip> playingClips = new ArrayList<>();

    public void onVisualConfig(@Observes VisualConfig visualConfig) {
        clips.clear();
        for (Map.Entry<Integer, ClipConfig> entry : clips.entrySet()) {
            clips.put(entry.getKey(), entry.getValue());
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

    @Override
    public void playClip(Vertex position, Vertex norm, int clipId, long timeStamp) {
        clips.get(clipId);
        synchronized (playingClips) {
            playingClips.add(new PlayingClip(position, norm, getClipConfig(clipId), timeStamp));
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
}
