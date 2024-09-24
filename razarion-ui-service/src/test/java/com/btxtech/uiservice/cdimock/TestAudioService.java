package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.uiservice.audio.AudioService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Singleton;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.01.2017.
 */
@Singleton
public class TestAudioService extends AudioService {

    @Inject
    public TestAudioService(AlarmService alarmService, ItemTypeService itemTypeService, TerrainUiService terrainUiService) {
        super(alarmService, itemTypeService, terrainUiService);
    }

    @Override
    protected void playAudio(int audioId) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void playTerrainLoopAudio(int audioId, double volume) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void muteTerrainLoopAudio() {
        throw new UnsupportedOperationException();
    }
}
