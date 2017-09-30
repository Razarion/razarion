package com.btxtech.webglemulator.razarion;

import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.uiservice.inventory.InventoryUiService;

import javax.inject.Singleton;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 29.09.2017.
 */
@Singleton
public class DevToolInventoryUiService extends InventoryUiService {
    @Override
    protected void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        throw new UnsupportedOperationException();
    }
}
