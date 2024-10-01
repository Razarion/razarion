package com.btxtech.uiservice.audio;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.system.alarm.AlarmService;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.inject.Provider;
import java.util.logging.Logger;

import static com.btxtech.shared.system.alarm.Alarm.Type.INVALID_AUDIO_SERVICE;

/**
 * Created by Beat
 * 24.12.2016.
 */
public abstract class AudioService /* implements ViewService.ViewFieldListener */ {
    private final Logger logger = Logger.getLogger(AudioService.class.getName());

    private final Provider<TerrainUiService> terrainUiService;

    private final ItemTypeService itemTypeService;

    private final AlarmService alarmService;
    private AudioConfig audioConfig;
    private double lastLandWaterProportion = -1;

    public AudioService(AlarmService alarmService, ItemTypeService itemTypeService, Provider<TerrainUiService> terrainUiService) {
        this.alarmService = alarmService;
        this.itemTypeService = itemTypeService;
        this.terrainUiService = terrainUiService;
    }

    protected abstract void playAudio(int audioId);

    protected abstract void playTerrainLoopAudio(int audioId, double volume);

    public abstract void muteTerrainLoopAudio();


    public void onGameUiControlInitEvent(GameUiControlInitEvent gameUiControlInitEvent) {
        this.audioConfig = gameUiControlInitEvent.getColdGameUiContext().getAudioConfig();
    }

    public void playAudioSafe(Integer audioItemConfigId) {
        if (audioItemConfigId != null) {
            playAudio(audioItemConfigId);
        }
    }

    public void onDialogOpened(Integer audioId) {
        playAudioSafe(audioId);
    }

    public void onDialogClosed() {
        if (getAudioConfig().getDialogClosed() != null) {
            playAudio(getAudioConfig().getDialogClosed());
        }
    }

    public void onQuestActivated() {
        if (getAudioConfig().getOnQuestActivated() != null) {
            playAudio(getAudioConfig().getOnQuestActivated());
        }
    }

    public void onSpawnSyncItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
        Integer audioId = baseItemType.getSpawnAudioId();
        if (audioId == null) {
        }
        // TODO if (!nativeSyncBaseItemTickInfo.contained) {
        // TODO    if (viewService.getCurrentViewField().isInside(NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo))) {
        // TODO        playAudio(audioId);
        // TODO    }
        // TODO }
    }

    public void onCommandSent() {
        if (getAudioConfig().getOnCommandSent() != null) {
            playAudio(getAudioConfig().getOnCommandSent());
        }
    }

    public void onSelectionChanged(SelectionEvent selectionEvent) {
        if (selectionEvent.isSuppressAudio()) {
            return;
        }
        switch (selectionEvent.getType()) {

            case CLEAR:
                if (getAudioConfig().getOnSelectionCleared() != null) {
                    playAudio(getAudioConfig().getOnSelectionCleared());
                }
                break;
            case OWN:
                if (selectionEvent.getSelectedGroup().getCount() > 1) {
                    if (getAudioConfig().getOnOwnMultiSelection() != null) {
                        playAudio(getAudioConfig().getOnOwnMultiSelection());
                    }
                } else {
                    if (getAudioConfig().getOnOwnSingleSelection() != null) {
                        playAudio(getAudioConfig().getOnOwnSingleSelection());
                    }
                }
                break;
            case OTHER:
                if (getAudioConfig().getOnOtherSelection() != null) {
                    playAudio(getAudioConfig().getOnOtherSelection());
                }
                break;
        }
    }

    public AudioConfig getAudioConfig() {
        if (audioConfig == null) {
            logger.warning("Using Fallback. No audio config.");
            return new AudioConfig();
        }
        return audioConfig;
    }

    // TODO @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        if (getAudioConfig().getTerrainLoopLand() == null) {
            alarmService.riseAlarm(INVALID_AUDIO_SERVICE, "TerrainLoopLand");
            return;
        }
        if (getAudioConfig().getTerrainLoopWater() == null) {
            alarmService.riseAlarm(INVALID_AUDIO_SERVICE, "TerrainLoopWater");
            return;
        }

        double landWaterProportion = terrainUiService.get().calculateLandWaterProportion();
        if (MathHelper.compareWithPrecision(lastLandWaterProportion, landWaterProportion, 0.05)) {
            return;
        }
        if (MathHelper.compareWithPrecision(landWaterProportion, 0.0, 0.05)) {
            landWaterProportion = 0;
        } else if (MathHelper.compareWithPrecision(landWaterProportion, 1.0, 0.05)) {
            landWaterProportion = 1;
        }

        lastLandWaterProportion = landWaterProportion;

        playTerrainLoopAudio(getAudioConfig().getTerrainLoopLand(), lastLandWaterProportion);
        playTerrainLoopAudio(getAudioConfig().getTerrainLoopWater(), 1.0 - lastLandWaterProportion);
    }
}
