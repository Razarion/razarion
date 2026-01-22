package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.uiservice.Colors;

import jakarta.inject.Singleton;
import jakarta.inject.Inject;

/**
 * Created by Beat
 * 13.02.2017.
 */
@Singleton
public class ItemUiService {

    private BaseItemUiService baseItemUiService;

    private ResourceUiService resourceUiService;

    private BoxUiService boxUiService;

    @Inject
    public ItemUiService(BoxUiService boxUiService, ResourceUiService resourceUiService, BaseItemUiService baseItemUiService) {
        this.boxUiService = boxUiService;
        this.resourceUiService = resourceUiService;
        this.baseItemUiService = baseItemUiService;
    }

    public SyncItemMonitor monitorSyncItem(SyncItemSimpleDto syncItem) {
        if (syncItem instanceof SyncBoxItemSimpleDto) {
            return boxUiService.monitorSyncBoxItem((SyncBoxItemSimpleDto) syncItem);
        } else if (syncItem instanceof SyncBaseItemSimpleDto) {
            return baseItemUiService.monitorSyncItem(syncItem.getId());
        } else if (syncItem instanceof SyncResourceItemSimpleDto) {
            return resourceUiService.monitorSyncResourceItem((SyncResourceItemSimpleDto) syncItem);
        } else {
            throw new IllegalArgumentException("Don't know how to handle: " + syncItem);
        }
    }

    public Color color4SyncItem(SyncItemSimpleDto syncItem) {
        if (syncItem instanceof SyncBoxItemSimpleDto) {
            return Colors.NONE_BASE;
        } else if (syncItem instanceof SyncBaseItemSimpleDto) {
            SyncBaseItemSimpleDto syncBaseItemSimpleDto = (SyncBaseItemSimpleDto) syncItem;
            if (baseItemUiService.isMyOwnProperty(syncBaseItemSimpleDto)) {
                return Colors.OWN;
            } else if (baseItemUiService.isMyEnemy(syncBaseItemSimpleDto)) {
                return Colors.ENEMY;
            } else {
                return Colors.FRIEND;
            }
        } else if (syncItem instanceof SyncResourceItemSimpleDto) {
            return Colors.NONE_BASE;
        } else {
            throw new IllegalArgumentException("Don't know how to handle: " + syncItem);
        }
    }
}
