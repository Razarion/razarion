package com.btxtech.uiservice.item;

import com.btxtech.shared.datatypes.Color;
import com.btxtech.shared.gameengine.datatypes.workerdto.NativeSyncBaseItemTickInfo;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBoxItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncItemSimpleDto;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncResourceItemSimpleDto;
import com.btxtech.uiservice.Colors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * Created by Beat
 * 13.02.2017.
 */
@ApplicationScoped
public class ItemUiService {
    @Inject
    private BaseItemUiService baseItemUiService;
    @Inject
    private ResourceUiService resourceUiService;
    @Inject
    private BoxUiService boxUiService;

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
