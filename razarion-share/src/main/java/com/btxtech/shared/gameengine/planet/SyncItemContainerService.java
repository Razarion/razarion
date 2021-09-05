package com.btxtech.shared.gameengine.planet;

import com.btxtech.shared.datatypes.DecimalPosition;
import com.btxtech.shared.gameengine.planet.model.SyncItem;

import java.util.function.Consumer;

public interface SyncItemContainerService {
    void iterateCellRadiusItem(DecimalPosition center, double radius, Consumer<SyncItem> callback);
}
