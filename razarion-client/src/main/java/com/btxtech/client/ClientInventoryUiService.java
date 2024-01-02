package com.btxtech.client;

import com.btxtech.common.system.ClientExceptionHandlerImpl;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.rest.InventoryController;
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
@Deprecated
public class ClientInventoryUiService  {
    // private Logger logger = Logger.getLogger(ClientInventoryUiService.class.getName());
    @Inject
    private Caller<InventoryController> inventoryController;
    @Inject
    private ClientExceptionHandlerImpl exceptionHandler;

    protected void loadServerInventoryInfo(Consumer<InventoryInfo> inventoryInfoConsumer) {
        inventoryController.call((RemoteCallback<InventoryInfo>) inventoryInfoConsumer::accept, exceptionHandler.restErrorHandler("InventoryProvider.loadInventory()")).loadInventory();
    }
}
