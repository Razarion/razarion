package com.btxtech.uiservice.audio;

import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.gameengine.planet.model.SyncBaseItem;
import com.btxtech.shared.gameengine.planet.terrain.TerrainService;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainScrollHandler;
import com.btxtech.uiservice.terrain.TerrainScrollListener;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.12.2016.
 */
public abstract class AudioService implements TerrainScrollListener {
    private static final double TERRAIN_LAND_LOOP_THRESHOLD = 0.3;
    @Inject
    private TerrainScrollHandler terrainScrollHandler;
    @Inject
    private TerrainService terrainService;
    private AudioConfig audioConfig;
    private Integer lastTerrainLoopAudio;

    protected abstract void playAudio(int audioId);

    protected abstract void playTerrainLoopAudio(int audioId);

    @PostConstruct
    public void postConstruct() {
        terrainScrollHandler.addTerrainScrollListener(this);
    }

    public void initialise(AudioConfig audioConfig) {
        this.audioConfig = audioConfig;
    }

    public void onDialogOpened(Integer audioId) {
        if (audioId != null) {
            playAudio(audioId);
        }
    }

    public void onDialogClosed() {
        if (audioConfig.getDialogClosed() != null) {
            playAudio(audioConfig.getDialogClosed());
        }
    }

    public void onQuestActivated() {
        if (audioConfig.getOnQuestActivated() != null) {
            playAudio(audioConfig.getOnQuestActivated());
        }
    }

    public void onSpawnSyncItem(SyncBaseItem syncBaseItem) {
        Integer audioId = syncBaseItem.getBaseItemType().getSpawnAudioId();
        if (audioId == null) {
            return;
        }
        if (terrainScrollHandler.getCurrentViewField().isInside(syncBaseItem.getSyncPhysicalArea().getPosition2d())) {
            playAudio(audioId);
        }
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    @Override
    public void onScroll(ViewField viewField) {
        double landWaterProportion = terrainService.calculateLandWaterProportion(viewField.calculateAabbRectangle());
        int terrainLoopAudio = landWaterProportion > TERRAIN_LAND_LOOP_THRESHOLD ? 272521 : 272522;
        if (lastTerrainLoopAudio != null && lastTerrainLoopAudio == terrainLoopAudio) {
            return;
        }
        lastTerrainLoopAudio = terrainLoopAudio;
        playTerrainLoopAudio(terrainLoopAudio);
    }
}
