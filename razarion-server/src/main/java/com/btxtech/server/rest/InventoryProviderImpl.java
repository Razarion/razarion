package com.btxtech.server.rest;

import com.btxtech.server.gameengine.ServerInventoryService;
import com.btxtech.server.web.SessionHolder;
import com.btxtech.shared.dto.InventoryInfo;
import com.btxtech.shared.rest.InventoryProvider;
import com.btxtech.shared.system.ExceptionHandler;

import javax.inject.Inject;

/**
 * Created by Beat
 * on 18.09.2017.
 */
public class InventoryProviderImpl implements InventoryProvider {
    @Inject
    private ExceptionHandler exceptionHandler;
    @Inject
    private ServerInventoryService serverInventoryService;
    @Inject
    private SessionHolder sessionHolder;

    @Override
    public InventoryInfo loadInventory() {
        try {
            return serverInventoryService.loadInventory(sessionHolder.getPlayerSession());
        } catch (Throwable e) {
            exceptionHandler.handleException(e);
            throw e;
        }
    }
}