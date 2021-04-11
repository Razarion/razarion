package com.btxtech.uiservice.cockpit.item;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;
import com.btxtech.shared.gameengine.datatypes.workerdto.SyncBaseItemSimpleDto;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Beat
 * 29.09.2016.
 */
@Deprecated
public interface OwnMultiDifferentItemPanel {
    void init(Map<BaseItemType, Collection<SyncBaseItemSimpleDto>> itemTypes);
}
