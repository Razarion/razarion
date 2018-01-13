package com.btxtech.uiservice.audio;

import com.btxtech.shared.datatypes.Rectangle2D;
import com.btxtech.shared.dto.AudioConfig;
import com.btxtech.shared.gameengine.ItemTypeService;
import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeUtil;
import com.btxtech.shared.utils.MathHelper;
import com.btxtech.uiservice.SelectionEvent;
import com.btxtech.uiservice.control.GameUiControlInitEvent;
import com.btxtech.uiservice.renderer.ViewField;
import com.btxtech.uiservice.renderer.ViewService;
import com.btxtech.uiservice.terrain.TerrainUiService;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Created by Beat
 * 24.12.2016.
 */
public abstract class AudioService implements ViewService.ViewFieldListener {
    @Inject
    private ViewService viewService;
    @Inject
    private TerrainUiService terrainUiService;
    @Inject
    private ItemTypeService itemTypeService;
    private AudioConfig audioConfig;
    private double lastLandWaterProportion = -1;

    protected abstract void playAudio(int audioId);

    protected abstract void playTerrainLoopAudio(int audioId, double volume);

    public abstract void muteTerrainLoopAudio();


    @PostConstruct
    public void postConstruct() {
        viewService.addViewFieldListeners(this);
    }

    public void onGameUiControlInitEvent(@Observes GameUiControlInitEvent gameUiControlInitEvent) {
        this.audioConfig = gameUiControlInitEvent.getColdGameUiControlConfig().getAudioConfig();
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

    public void onSpawnSyncItem(NativeSyncBaseItemTickInfo nativeSyncBaseItemTickInfo) {
        BaseItemType baseItemType = itemTypeService.getBaseItemType(nativeSyncBaseItemTickInfo.itemTypeId);
        Integer audioId = baseItemType.getSpawnAudioId();
        if (audioId == null) {
            return;
        }
        if (!nativeSyncBaseItemTickInfo.contained) {
            if (viewService.getCurrentViewField().isInside(NativeUtil.toSyncBaseItemPosition2d(nativeSyncBaseItemTickInfo))) {
                playAudio(audioId);
            }
        }
    }

    public void onParticle(int audioId) {
        playAudio(audioId);
    }

    public void onCommandSent() {
        if (audioConfig.getOnCommandSent() != null) {
            playAudio(audioConfig.getOnCommandSent());
        }
    }

    public void onSelectionChanged(@Observes SelectionEvent selectionEvent) {
        if (selectionEvent.isSuppressAudio()) {
            return;
        }
        switch (selectionEvent.getType()) {

            case CLEAR:
                if (audioConfig.getOnSelectionCleared() != null) {
                    playAudio(audioConfig.getOnSelectionCleared());
                }
                break;
            case OWN:
                if (selectionEvent.getSelectedGroup().getCount() > 1) {
                    if (audioConfig.getOnOwnMultiSelection() != null) {
                        playAudio(audioConfig.getOnOwnMultiSelection());
                    }
                } else {
                    if (audioConfig.getOnOwnSingleSelection() != null) {
                        playAudio(audioConfig.getOnOwnSingleSelection());
                    }
                }
                break;
            case OTHER:
                if (audioConfig.getOnOtherSelection() != null) {
                    playAudio(audioConfig.getOnOtherSelection());
                }
                break;
        }
    }

    public AudioConfig getAudioConfig() {
        return audioConfig;
    }

    @Override
    public void onViewChanged(ViewField viewField, Rectangle2D absAabbRect) {
        double landWaterProportion = terrainUiService.calculateLandWaterProportion();
        if (MathHelper.compareWithPrecision(lastLandWaterProportion, landWaterProportion, 0.05)) {
            return;
        }
        if (MathHelper.compareWithPrecision(landWaterProportion, 0.0, 0.05)) {
            landWaterProportion = 0;
        } else if (MathHelper.compareWithPrecision(landWaterProportion, 1.0, 0.05)) {
            landWaterProportion = 1;
        }

        lastLandWaterProportion = landWaterProportion;

        playTerrainLoopAudio(audioConfig.getTerrainLoopLand(), lastLandWaterProportion);
        playTerrainLoopAudio(audioConfig.getTerrainLoopWater(), 1.0 - lastLandWaterProportion);
    }
}
