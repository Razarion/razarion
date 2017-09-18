package com.btxtech.client;

import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.rest.InventoryProvider;
import com.btxtech.uiservice.inventory.InventoryUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Beat
 * on 17.09.2017.
 */
@ApplicationScoped
public class ClientInventoryUiService extends InventoryUiService {
    private Logger logger = Logger.getLogger(ClientInventoryUiService.class.getName());
    @Inject
    private Caller<InventoryProvider> inventoryProvider;

    @Override
    protected void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        inventoryProvider.call((RemoteCallback<InventoryInfo>) inventoryInfoConsumer::accept, (message, throwable) -> {
            logger.log(Level.SEVERE, "InventoryProvider.loadInventory() failed: " + message, throwable);
            return false;
        }).loadInventory();
    }
}
