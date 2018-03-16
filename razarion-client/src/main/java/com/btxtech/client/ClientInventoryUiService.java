package com.btxtech.client;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.rest.InventoryProvider;
import com.btxtech.uiservice.inventory.InventoryUiService;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by Beat
 * on 17.09.2017.
 */
@ApplicationScoped
public class ClientInventoryUiService extends InventoryUiService {
    // private Logger logger = Logger.getLogger(ClientInventoryUiService.class.getName());
    @Inject
    private Caller<InventoryProvider> inventoryProvider;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    @Override
    protected void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        inventoryProvider.call((RemoteCallback<InventoryInfo>) inventoryInfoConsumer::accept, exceptionHandler.restErrorHandler("InventoryProvider.loadInventory()")).loadInventory();
    }
}
