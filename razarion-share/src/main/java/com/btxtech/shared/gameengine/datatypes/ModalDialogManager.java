package com.btxtech.shared.gameengine.datatypes;

import com.btxtech.shared.gameengine.datatypes.itemtype.BaseItemType;

/**
 * Created by Beat
 * 26.10.2016.
 */
@Deprecated
public interface ModalDialogManager {
    void showBoxPicked(BoxContent boxContent);

    void showUseInventoryItemLimitExceeded(BaseItemType baseItemType);

    void showUseInventoryHouseSpaceExceeded();
}
