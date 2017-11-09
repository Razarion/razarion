package com.btxtech.uiservice.cdimock;

import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.uiservice.inventory.InventoryUiService;

import javax.enterprise.context.ApplicationScoped;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 09.11.2017.
 */
@ApplicationScoped
public class TestInventoryUiService extends InventoryUiService {
    @Override
    protected void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        throw new UnsupportedOperationException();
    }
}
